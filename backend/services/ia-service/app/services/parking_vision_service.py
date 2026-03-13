# ia-service/app/services/parking_vision_service.py

"""
Servicio de visión de estacionamiento (Parking Vision).

Este servicio orquesta la detección de ocupación de espacios y el reconocimiento
de placas desde vista aérea. Integra:
- ParkingOccupancyDetector (detección de vehículos y ocupación)
- PlateRecognitionService (OCR de placas)
- S3Service (descarga/upload de imágenes)

Flujo:
1. Descarga imagen desde S3
2. Detecta ocupación de espacios (YOLO)
3. Para cada espacio ocupado, intenta leer placa (OCR)
4. Opcionalmente guarda imagen anotada en S3
5. Retorna resultado completo
"""

import logging
import time
from datetime import datetime
from pathlib import Path
from typing import Dict, List, Optional, Tuple
from io import BytesIO

import cv2
import numpy as np
from pydantic import BaseModel, Field, validator

from app.ml.dl.parking import (
    ParkingOccupancyDetector,
    ParkingDetectionResult,
    create_detector_instance
)
from app.services.plate_recognition_service import PlateRecognitionService
from app.core.config import settings

logger = logging.getLogger(__name__)


# ============================================================================
# MODELOS DE DATOS (Request/Response)
# ============================================================================

class ParkingVisionRequest(BaseModel):
    """
    Request para detección de visión de estacionamiento.
    """
    parking_id: int = Field(..., gt=0, description="ID del estacionamiento")
    image_url: str = Field(..., description="URL de la imagen en S3 (s3://bucket/key)")
    polygons: Dict[str, List[List[float]]] = Field(
        ...,
        description="Polígonos de espacios {space_id: [[x,y], ...]}"
    )

    # Opcionales
    save_annotated_image: bool = Field(
        default=False,
        description="Si guardar imagen anotada en S3"
    )
    detect_plates: bool = Field(
        default=True,
        description="Si intentar leer placas desde vista aérea"
    )
    plate_confidence_threshold: float = Field(
        default=0.70,
        ge=0.0,
        le=1.0,
        description="Umbral de confianza para OCR (menor que frontal por ángulo aéreo)"
    )

    @validator('polygons')
    def validate_polygons_not_empty(cls, v):
        """Valida que haya al menos un polígono"""
        if not v:
            raise ValueError("Debe proporcionar al menos un polígono")
        return v

    @validator('image_url')
    def validate_s3_url(cls, v):
        """Valida que la URL sea de S3"""
        if not v.startswith('s3://'):
            raise ValueError("La URL debe comenzar con 's3://'")
        return v


class SpaceVisionDetection(BaseModel):
    """
    Detección de visión para un espacio individual.
    """
    space_id: str
    occupied: bool
    vehicle_detected: bool
    vehicle_type: Optional[str] = None
    vehicle_confidence: float = 0.0

    # Placa (puede ser None si no se detectó o detect_plates=False)
    plate_number: Optional[str] = None
    plate_confidence: float = 0.0
    plate_detected: bool = False


class ParkingVisionResponse(BaseModel):
    """
    Response de detección de visión de estacionamiento.
    """
    parking_id: int
    snapshot_id: str
    image_url: str
    annotated_image_url: Optional[str] = None

    # Estadísticas
    total_spaces: int
    occupied_count: int
    available_count: int
    plates_detected_count: int

    # Detecciones por espacio
    detections: List[SpaceVisionDetection]

    # Metadata
    processed_at: str
    processing_time_ms: float


# ============================================================================
# SERVICIO PRINCIPAL
# ============================================================================

class ParkingVisionService:
    """
    Servicio principal de visión de estacionamiento.

    Orquesta la detección de ocupación y reconocimiento de placas
    desde vista aérea (ESP32-CAM o dron).

    Args:
        occupancy_detector: Detector de ocupación (inyectado)
        plate_service: Servicio de reconocimiento de placas (inyectado)
    """

    def __init__(
            self,
            occupancy_detector: Optional[ParkingOccupancyDetector] = None,
            plate_service: Optional[PlateRecognitionService] = None
    ):
        # Usar instancias inyectadas o crear nuevas
        self.occupancy_detector = occupancy_detector or create_detector_instance(
            model_path=settings.YOLO_MODEL_PATH,
            confidence_threshold=0.25,
            device=settings.DEVICE
        )

        self.plate_service = plate_service or PlateRecognitionService()

        logger.info("ParkingVisionService inicializado")

    async def process_parking_vision(
            self,
            request: ParkingVisionRequest
    ) -> ParkingVisionResponse:
        """
        Procesa una imagen de estacionamiento completa.

        Args:
            request: ParkingVisionRequest con imagen, polígonos y config

        Returns:
            ParkingVisionResponse con detecciones completas

        Raises:
            ValueError: Si hay errores en la imagen o polígonos
            Exception: Errores durante procesamiento
        """
        start_time = time.time()

        logger.info(
            f"📸 Procesando parking_id={request.parking_id}, "
            f"espacios={len(request.polygons)}, "
            f"detect_plates={request.detect_plates}"
        )

        try:
            # PASO 1: Descargar imagen desde S3
            image = await self._download_image_from_s3(request.image_url)
            logger.info(f"Imagen descargada: {image.shape}")

            # PASO 2: Detectar ocupación de espacios
            occupancy_result = self.occupancy_detector.detect_occupancy(
                image=image,
                polygons_dict=request.polygons,
                return_annotated_image=request.save_annotated_image
            )
            logger.info(
                f"Ocupación detectada: {occupancy_result.occupied_count}/"
                f"{occupancy_result.total_spaces} ocupados"
            )

            # PASO 3: Reconocer placas (si está habilitado)
            detections_with_plates = []
            plates_detected_count = 0

            for detection in occupancy_result.detections:
                space_vision = SpaceVisionDetection(
                    space_id=detection.space_id,
                    occupied=detection.occupied,
                    vehicle_detected=detection.vehicle_detected,
                    vehicle_type=detection.vehicle_type,
                    vehicle_confidence=detection.vehicle_confidence
                )

                # Intentar leer placa si hay vehículo y está habilitado
                if (
                        request.detect_plates
                        and detection.vehicle_detected
                        and detection.plate_region is not None
                ):
                    plate_result = await self._recognize_plate_from_region(
                        detection.plate_region,
                        request.plate_confidence_threshold
                    )

                    if plate_result:
                        space_vision.plate_number = plate_result['plate']
                        space_vision.plate_confidence = plate_result['confidence']
                        space_vision.plate_detected = True
                        plates_detected_count += 1

                        logger.debug(
                            f"  {detection.space_id}: {plate_result['plate']} "
                            f"(conf: {plate_result['confidence']:.2f})"
                        )

                detections_with_plates.append(space_vision)

            if request.detect_plates:
                logger.info(
                    f"Placas detectadas: {plates_detected_count}/"
                    f"{occupancy_result.occupied_count} vehículos"
                )

            # PASO 4: Guardar imagen anotada en S3 (si se solicitó)
            annotated_image_url = None
            if request.save_annotated_image and occupancy_result.annotated_image is not None:
                annotated_image_url = await self._upload_annotated_image(
                    occupancy_result.annotated_image,
                    request.parking_id
                )
                logger.info(f"Imagen anotada guardada: {annotated_image_url}")

            # PASO 5: Generar response
            snapshot_id = self._generate_snapshot_id(request.parking_id)
            processing_time_ms = (time.time() - start_time) * 1000

            response = ParkingVisionResponse(
                parking_id=request.parking_id,
                snapshot_id=snapshot_id,
                image_url=request.image_url,
                annotated_image_url=annotated_image_url,
                total_spaces=occupancy_result.total_spaces,
                occupied_count=occupancy_result.occupied_count,
                available_count=occupancy_result.available_count,
                plates_detected_count=plates_detected_count,
                detections=detections_with_plates,
                processed_at=datetime.utcnow().isoformat(),
                processing_time_ms=round(processing_time_ms, 2)
            )

            logger.info(
                f"Procesamiento completo en {processing_time_ms:.2f}ms: "
                f"{occupancy_result.occupied_count} ocupados, "
                f"{plates_detected_count} placas detectadas"
            )

            return response

        except Exception as e:
            logger.error(f"Error procesando parking vision: {e}", exc_info=True)
            raise

    # ========================================================================
    # MÉTODOS PRIVADOS
    # ========================================================================

    async def _download_image_from_s3(self, s3_url: str) -> np.ndarray:
        """
        Descarga imagen desde S3 y la convierte a numpy array BGR.

        Args:
            s3_url: URL S3 (formato: s3://bucket/key)

        Returns:
            Imagen como numpy array BGR

        Raises:
            ValueError: Si la URL es inválida o la imagen no se puede leer
        """
        try:
            # Parsear URL S3: s3://bucket/key
            if not s3_url.startswith('s3://'):
                raise ValueError(f"URL S3 inválida: {s3_url}")

            s3_path = s3_url[5:]  # Remover 's3://'
            parts = s3_path.split('/', 1)

            if len(parts) != 2:
                raise ValueError(f"URL S3 mal formada: {s3_url}")

            bucket_name, object_key = parts

            # Importar S3 service (lazy import para evitar dependencias circulares)
            from app.services.s3_service import S3Service

            s3_service = S3Service()

            # Descargar imagen
            logger.debug(f"Descargando desde S3: bucket={bucket_name}, key={object_key}")
            image_bytes = await s3_service.download_file(bucket_name, object_key)

            # Convertir bytes a numpy array
            nparr = np.frombuffer(image_bytes, np.uint8)
            image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

            if image is None:
                raise ValueError(f"No se pudo decodificar la imagen desde {s3_url}")

            return image

        except Exception as e:
            logger.error(f"Error descargando imagen desde S3: {e}")
            raise ValueError(f"Error descargando imagen: {e}")

    async def _recognize_plate_from_region(
            self,
            plate_region: np.ndarray,
            confidence_threshold: float
    ) -> Optional[Dict]:
        """
        Intenta reconocer placa desde una región recortada.

        NOTA: Vista aérea tiene menor precisión que vista frontal.
        Por eso usamos un threshold más bajo (0.70 vs 0.85).

        Args:
            plate_region: Región de imagen con vehículo
            confidence_threshold: Umbral de confianza

        Returns:
            Dict con {plate, confidence} o None si no se detectó
        """
        try:
            # El PlateRecognitionService existente espera bytes
            # Convertir numpy array a bytes
            _, buffer = cv2.imencode('.jpg', plate_region)
            image_bytes = buffer.tobytes()

            # Reconocer placa usando servicio existente
            result = await self.plate_service.recognize_plate(image_bytes)

            # Verificar confianza
            if result and result.get('confidence', 0) >= confidence_threshold:
                return {
                    'plate': result['plate'],
                    'confidence': result['confidence']
                }

            return None

        except Exception as e:
            logger.debug(f"No se pudo reconocer placa desde región: {e}")
            return None

    async def _upload_annotated_image(
            self,
            annotated_image: np.ndarray,
            parking_id: int
    ) -> str:
        """
        Guarda imagen anotada en S3.

        Args:
            annotated_image: Imagen con anotaciones (numpy array)
            parking_id: ID del parking

        Returns:
            URL S3 de la imagen guardada
        """
        try:
            # Generar nombre único para la imagen
            timestamp = datetime.utcnow().strftime("%Y%m%d-%H%M%S")
            object_key = f"parking-{parking_id}/annotated/{timestamp}.jpg"

            # Convertir numpy array a bytes
            _, buffer = cv2.imencode('.jpg', annotated_image)
            image_bytes = buffer.tobytes()

            # Importar S3 service
            from app.services.s3_service import S3Service

            s3_service = S3Service()

            # Upload a S3
            bucket_name = settings.S3_BUCKET_NAME  # Desde config
            await s3_service.upload_file(
                bucket_name=bucket_name,
                object_key=object_key,
                file_data=image_bytes,
                content_type='image/jpeg'
            )

            # Retornar URL S3
            s3_url = f"s3://{bucket_name}/{object_key}"
            return s3_url

        except Exception as e:
            logger.error(f"Error guardando imagen anotada en S3: {e}")
            # No falla el proceso completo si no se puede guardar
            return None

    def _generate_snapshot_id(self, parking_id: int) -> str:
        """
        Genera un ID único para el snapshot.

        Args:
            parking_id: ID del parking

        Returns:
            ID único (formato: snapshot-{parking_id}-{timestamp})
        """
        timestamp = datetime.utcnow().strftime("%Y%m%d-%H%M%S-%f")[:-3]  # Incluir ms
        return f"snapshot-{parking_id}-{timestamp}"


# ============================================================================
# INSTANCIA SINGLETON (para usar en endpoints)
# ============================================================================

# Instancia global del servicio (se inicializa al importar)
_parking_vision_service_instance: Optional[ParkingVisionService] = None


def get_parking_vision_service() -> ParkingVisionService:
    """
    Obtiene instancia singleton del servicio.

    Returns:
        ParkingVisionService
    """
    global _parking_vision_service_instance

    if _parking_vision_service_instance is None:
        _parking_vision_service_instance = ParkingVisionService()

    return _parking_vision_service_instance