# ia-service/app/ml/dl/parking/occupancy_detector.py

"""
Detector de ocupación de estacionamiento usando Ultralytics YOLO26.

Este módulo integra ParkingManagement de Ultralytics para detectar
espacios de estacionamiento ocupados/disponibles mediante análisis de video/imágenes.

Características:
- Detección de vehículos en tiempo real
- Mapeo de vehículos a espacios (polígonos)
- Soporte para polígonos dinámicos (no requiere archivo JSON estático)
- Compatible con vista aérea desde ESP32-CAM o dron
"""

import json
import logging
import tempfile
import time
from pathlib import Path
from typing import Dict, List, Optional, Tuple

import cv2
import numpy as np
from ultralytics import YOLO, solutions

from app.utils.polygon_utils import (
    ParkingPolygonSet,
    load_polygons_from_dict,
    point_in_polygon,
    get_polygon_center,
)

logger = logging.getLogger(__name__)


# ============================================================================
# MODELOS DE DATOS PARA DETECCIÓN
# ============================================================================

class SpaceDetection:
    """
    Representa la detección de ocupación de un espacio individual.
    """

    def __init__(
            self,
            space_id: str,
            occupied: bool,
            vehicle_detected: bool = False,
            vehicle_type: Optional[str] = None,
            vehicle_confidence: float = 0.0,
            vehicle_bbox: Optional[Tuple[int, int, int, int]] = None,
            plate_region: Optional[np.ndarray] = None
    ):
        self.space_id = space_id
        self.occupied = occupied
        self.vehicle_detected = vehicle_detected
        self.vehicle_type = vehicle_type
        self.vehicle_confidence = vehicle_confidence
        self.vehicle_bbox = vehicle_bbox  # (x1, y1, x2, y2)
        self.plate_region = plate_region  # Región recortada para OCR

    def to_dict(self) -> Dict:
        """Convierte a diccionario para respuesta API"""
        return {
            "spaceId": self.space_id,
            "occupied": self.occupied,
            "vehicleDetected": self.vehicle_detected,
            "vehicleType": self.vehicle_type,
            "vehicleConfidence": round(self.vehicle_confidence, 3) if self.vehicle_confidence else 0.0,
            # plate_region no se incluye (se usa internamente para OCR)
        }


class ParkingDetectionResult:
    """
    Resultado completo de la detección de ocupación de parking.
    """

    def __init__(
            self,
            total_spaces: int,
            occupied_count: int,
            available_count: int,
            detections: List[SpaceDetection],
            processing_time_ms: float,
            annotated_image: Optional[np.ndarray] = None
    ):
        self.total_spaces = total_spaces
        self.occupied_count = occupied_count
        self.available_count = available_count
        self.detections = detections
        self.processing_time_ms = processing_time_ms
        self.annotated_image = annotated_image

    def to_dict(self, include_image: bool = False) -> Dict:
        """Convierte a diccionario para respuesta API"""
        result = {
            "totalSpaces": self.total_spaces,
            "occupiedCount": self.occupied_count,
            "availableCount": self.available_count,
            "detections": [det.to_dict() for det in self.detections],
            "processingTimeMs": round(self.processing_time_ms, 2)
        }

        # La imagen anotada se maneja por separado (demasiado grande para JSON)
        # Se puede guardar en S3 si se necesita

        return result


# ============================================================================
# DETECTOR DE OCUPACIÓN
# ============================================================================

class ParkingOccupancyDetector:
    """
    Detector de ocupación de estacionamiento usando Ultralytics YOLO26.

    Este detector:
    1. Recibe imagen + polígonos dinámicamente
    2. Detecta vehículos con YOLO26
    3. Mapea vehículos a espacios de estacionamiento
    4. Retorna ocupación por espacio

    Args:
        model_path: Ruta al modelo YOLO (default: yolo26n.pt)
        confidence_threshold: Umbral de confianza para detecciones (default: 0.25)
        device: Dispositivo de inferencia - 'cpu', 'cuda:0', etc. (default: 'cpu')
    """

    # Clases de vehículos según COCO dataset (usado por YOLO)
    VEHICLE_CLASSES = {
        2: 'car',
        3: 'motorcycle',
        5: 'bus',
        7: 'truck'
    }

    def __init__(
            self,
            model_path: str = "yolo26n.pt",
            confidence_threshold: float = 0.25,
            device: str = "cpu"
    ):
        self.model_path = model_path
        self.confidence_threshold = confidence_threshold
        self.device = device

        # Cargar modelo YOLO
        logger.info(f"Cargando modelo YOLO desde {model_path}...")
        self.model = YOLO(model_path)
        logger.info(f"Modelo YOLO cargado en dispositivo: {device}")

    def detect_occupancy(
            self,
            image: np.ndarray,
            polygons_dict: Dict[str, List[List[float]]],
            return_annotated_image: bool = False
    ) -> ParkingDetectionResult:
        """
        Detecta ocupación de espacios de estacionamiento.

        Args:
            image: Imagen BGR (numpy array)
            polygons_dict: Diccionario de polígonos {space_id: [[x,y], ...]}
            return_annotated_image: Si retornar imagen con anotaciones (default: False)

        Returns:
            ParkingDetectionResult con detecciones por espacio

        Raises:
            ValueError: Si la imagen o polígonos son inválidos
        """
        start_time = time.time()

        # Validar entrada
        if image is None or image.size == 0:
            raise ValueError("Imagen inválida o vacía")

        if not polygons_dict:
            raise ValueError("Diccionario de polígonos vacío")

        logger.info(f"Procesando imagen {image.shape} con {len(polygons_dict)} espacios")

        # Cargar y validar polígonos
        polygon_set = load_polygons_from_dict(polygons_dict)

        # Detectar vehículos con YOLO
        vehicle_detections = self._detect_vehicles(image)
        logger.info(f"Detectados {len(vehicle_detections)} vehículos")

        # Mapear vehículos a espacios
        space_detections = self._map_vehicles_to_spaces(
            image,
            polygon_set,
            vehicle_detections
        )

        # Calcular estadísticas
        occupied_count = sum(1 for det in space_detections if det.occupied)
        available_count = len(space_detections) - occupied_count

        # Imagen anotada (opcional)
        annotated_image = None
        if return_annotated_image:
            annotated_image = self._draw_detections(
                image,
                polygon_set,
                space_detections
            )

        # Calcular tiempo de procesamiento
        processing_time_ms = (time.time() - start_time) * 1000

        result = ParkingDetectionResult(
            total_spaces=len(space_detections),
            occupied_count=occupied_count,
            available_count=available_count,
            detections=space_detections,
            processing_time_ms=processing_time_ms,
            annotated_image=annotated_image
        )

        logger.info(
            f"Detección completa: {occupied_count}/{len(space_detections)} ocupados "
            f"({processing_time_ms:.2f}ms)"
        )

        return result

    def _detect_vehicles(
            self,
            image: np.ndarray
    ) -> List[Dict]:
        """
        Detecta vehículos en la imagen usando YOLO.

        Args:
            image: Imagen BGR

        Returns:
            Lista de detecciones con formato:
            [
                {
                    'bbox': (x1, y1, x2, y2),
                    'confidence': 0.85,
                    'class_id': 2,
                    'class_name': 'car',
                    'center': (x_center, y_center)
                },
                ...
            ]
        """
        # Ejecutar inferencia YOLO
        results = self.model.predict(
            image,
            conf=self.confidence_threshold,
            device=self.device,
            verbose=False
        )

        vehicle_detections = []

        # Procesar resultados
        for result in results:
            boxes = result.boxes

            for box in boxes:
                class_id = int(box.cls[0])

                # Filtrar solo vehículos
                if class_id not in self.VEHICLE_CLASSES:
                    continue

                confidence = float(box.conf[0])
                x1, y1, x2, y2 = map(int, box.xyxy[0])

                # Calcular centro del vehículo
                center_x = (x1 + x2) // 2
                center_y = (y1 + y2) // 2

                vehicle_detections.append({
                    'bbox': (x1, y1, x2, y2),
                    'confidence': confidence,
                    'class_id': class_id,
                    'class_name': self.VEHICLE_CLASSES[class_id],
                    'center': (center_x, center_y)
                })

        return vehicle_detections

    def _map_vehicles_to_spaces(
            self,
            image: np.ndarray,
            polygon_set: ParkingPolygonSet,
            vehicle_detections: List[Dict]
    ) -> List[SpaceDetection]:
        """
        Mapea vehículos detectados a espacios de estacionamiento.

        Estrategia:
        - Si el CENTRO del vehículo está dentro de un polígono → espacio OCUPADO
        - Si no hay vehículo en el polígono → espacio DISPONIBLE

        Args:
            image: Imagen BGR original
            polygon_set: Conjunto de polígonos
            vehicle_detections: Lista de vehículos detectados

        Returns:
            Lista de SpaceDetection (uno por cada espacio)
        """
        space_detections = []

        for polygon in polygon_set.get_all_polygons():
            space_id = polygon.space_id

            # Buscar vehículo que esté dentro de este espacio
            vehicle_in_space = None

            for vehicle in vehicle_detections:
                center_x, center_y = vehicle['center']

                # Verificar si el centro del vehículo está en el polígono
                if point_in_polygon((center_x, center_y), polygon):
                    vehicle_in_space = vehicle
                    break  # Solo un vehículo por espacio

            # Crear detección de espacio
            if vehicle_in_space:
                # Espacio OCUPADO
                x1, y1, x2, y2 = vehicle_in_space['bbox']

                # Recortar región del vehículo para OCR posterior (CAPA 3)
                plate_region = image[y1:y2, x1:x2].copy()

                detection = SpaceDetection(
                    space_id=space_id,
                    occupied=True,
                    vehicle_detected=True,
                    vehicle_type=vehicle_in_space['class_name'],
                    vehicle_confidence=vehicle_in_space['confidence'],
                    vehicle_bbox=vehicle_in_space['bbox'],
                    plate_region=plate_region
                )
            else:
                # Espacio DISPONIBLE
                detection = SpaceDetection(
                    space_id=space_id,
                    occupied=False,
                    vehicle_detected=False
                )

            space_detections.append(detection)

        return space_detections

    def _draw_detections(
            self,
            image: np.ndarray,
            polygon_set: ParkingPolygonSet,
            space_detections: List[SpaceDetection]
    ) -> np.ndarray:
        """
        Dibuja detecciones en la imagen (para debugging/visualización).

        Args:
            image: Imagen BGR original
            polygon_set: Conjunto de polígonos
            space_detections: Detecciones por espacio

        Returns:
            Imagen anotada con polígonos, vehículos y estadísticas
        """
        from app.utils.polygon_utils import draw_polygons_on_image, draw_occupancy_stats

        # Crear mapa de ocupación para draw_polygons_on_image
        occupied_spaces = {
            det.space_id: det.occupied
            for det in space_detections
        }

        # Dibujar polígonos
        annotated = draw_polygons_on_image(
            image,
            polygon_set,
            occupied_spaces=occupied_spaces,
            show_labels=True,
            alpha=0.3
        )

        # Dibujar bounding boxes de vehículos
        for detection in space_detections:
            if detection.vehicle_detected and detection.vehicle_bbox:
                x1, y1, x2, y2 = detection.vehicle_bbox

                # Bounding box amarillo para vehículos
                cv2.rectangle(annotated, (x1, y1), (x2, y2), (0, 255, 255), 2)

                # Label con tipo de vehículo y confianza
                label = f"{detection.vehicle_type} {detection.vehicle_confidence:.2f}"
                cv2.putText(
                    annotated,
                    label,
                    (x1, y1 - 5),
                    cv2.FONT_HERSHEY_SIMPLEX,
                    0.5,
                    (0, 255, 255),
                    1,
                    cv2.LINE_AA
                )

        # Dibujar estadísticas de ocupación
        occupied_count = sum(1 for det in space_detections if det.occupied)
        available_count = len(space_detections) - occupied_count

        annotated = draw_occupancy_stats(
            annotated,
            total_spaces=len(space_detections),
            occupied_count=occupied_count,
            available_count=available_count
        )

        return annotated


# ============================================================================
# FUNCIONES DE UTILIDAD
# ============================================================================

def create_detector_instance(
        model_path: str = "yolo26n.pt",
        confidence_threshold: float = 0.25,
        device: str = "cpu"
) -> ParkingOccupancyDetector:
    """
    Factory function para crear instancia del detector.

    Args:
        model_path: Ruta al modelo YOLO
        confidence_threshold: Umbral de confianza
        device: Dispositivo de inferencia

    Returns:
        ParkingOccupancyDetector configurado
    """
    return ParkingOccupancyDetector(
        model_path=model_path,
        confidence_threshold=confidence_threshold,
        device=device
    )


def detect_from_file(
        image_path: str,
        polygons_dict: Dict[str, List[List[float]]],
        model_path: str = "yolo26n.pt",
        save_annotated: bool = False,
        output_path: Optional[str] = None
) -> ParkingDetectionResult:
    """
    Función de conveniencia para detectar desde archivo de imagen.

    Args:
        image_path: Ruta a la imagen
        polygons_dict: Diccionario de polígonos
        model_path: Ruta al modelo YOLO
        save_annotated: Si guardar imagen anotada
        output_path: Ruta para guardar imagen anotada

    Returns:
        ParkingDetectionResult
    """
    # Leer imagen
    image = cv2.imread(image_path)
    if image is None:
        raise ValueError(f"No se pudo leer la imagen: {image_path}")

    # Crear detector
    detector = create_detector_instance(model_path=model_path)

    # Detectar ocupación
    result = detector.detect_occupancy(
        image,
        polygons_dict,
        return_annotated_image=save_annotated
    )

    # Guardar imagen anotada si se solicitó
    if save_annotated and result.annotated_image is not None:
        output_path = output_path or "/tmp/parking_detection.jpg"
        cv2.imwrite(output_path, result.annotated_image)
        logger.info(f"Imagen anotada guardada en: {output_path}")

    return result