"""
Servicio principal de reconocimiento de placas vehiculares
Orquesta YOLO26 (detección) + EasyOCR (lectura) para reconocimiento completo
"""
import numpy as np
from typing import Optional, Dict
from datetime import datetime

from app.core.config import settings
from app.core.logging import logger
from app.core.constants import (
    STATUS_SUCCESS,
    STATUS_NO_PLATE_DETECTED,
    STATUS_OCR_FAILED,
    STATUS_LOW_CONFIDENCE,
    STATUS_INVALID_FORMAT
)
from app.core.exceptions import ImageProcessingException
from app.ml.dl.yolo.detector import YOLO26Detector
from app.ml.dl.ocr.reader import EasyOCRReader
from app.utils.image_utils import crop_image
from app.utils.validators import (
    validate_plate_format,
    normalize_plate_format,
    validate_confidence,
    calculate_ocr_quality_score
)
from app.utils.formatters import (
    format_detection_result,
    format_ocr_result,
    get_status_and_message,
    format_processing_time
)
from app.schemas.recognition import RecognitionResponse, RecognitionRequest


class PlateRecognitionService:
    """
    Servicio de reconocimiento de placas vehiculares

    Pipeline de procesamiento:
    1. Detección de placa con YOLO26 (CNN)
    2. Recorte de región de interés
    3. Lectura de texto con EasyOCR (CNN + RNN)
    4. Validación y normalización
    5. Construcción de respuesta
    """

    def __init__(self):
        """Inicializa el servicio de reconocimiento"""
        logger.info("Inicializando PlateRecognitionService")

        # Inicializar modelos de Deep Learning
        self.yolo_detector = YOLO26Detector()
        self.ocr_reader = EasyOCRReader()

        # Cargar modelos en memoria
        self._load_models()

    def _load_models(self) -> None:
        """Carga los modelos YOLO y OCR en memoria"""
        try:
            logger.info("Cargando modelos de Deep Learning...")

            # Cargar YOLO26
            self.yolo_detector.load_model()
            logger.info("✓ YOLO26 cargado")

            # Cargar EasyOCR
            self.ocr_reader.load_model()
            logger.info("✓ EasyOCR cargado")

            logger.info("Todos los modelos de Deep Learning cargados exitosamente")

        except Exception as e:
            logger.error(f"Error al cargar modelos: {str(e)}")
            raise

    def recognize_plate(
            self,
            image: np.ndarray,
            request: RecognitionRequest = RecognitionRequest()
    ) -> RecognitionResponse:
        """
        Reconoce una placa vehicular en una imagen

        Pipeline completo:
        Imagen → YOLO (detección) → Recorte → OCR (lectura) → Validación → Respuesta

        Args:
            image: Imagen en formato numpy array (BGR)
            request: Parámetros de configuración del reconocimiento

        Returns:
            RecognitionResponse con el resultado completo
        """
        start_time = datetime.now()

        logger.info("=== Iniciando reconocimiento de placa ===")

        try:
            # PASO 1: Detección con YOLO26 (CNN)
            logger.info("PASO 1: Detección YOLO26")
            detection = self._detect_plate(image, request.yolo_confidence)

            if detection is None:
                # No se detectó ninguna placa
                end_time = datetime.now()
                processing_time = format_processing_time(start_time, end_time)

                return RecognitionResponse(
                    status=STATUS_NO_PLATE_DETECTED,
                    message="No se detectó ninguna placa en la imagen",
                    plate_number=None,
                    detection=None,
                    ocr=None,
                    processing_time_ms=processing_time
                )

            # PASO 2: Recortar región de la placa
            logger.info("PASO 2: Recortando región de placa")
            plate_crop = crop_image(image, detection['bbox'])

            # PASO 3: Lectura con EasyOCR (CNN + RNN)
            logger.info("PASO 3: Lectura OCR")
            ocr_result = self._read_plate_text(plate_crop, request.ocr_confidence)

            if ocr_result is None:
                # OCR falló
                end_time = datetime.now()
                processing_time = format_processing_time(start_time, end_time)

                return RecognitionResponse(
                    status=STATUS_OCR_FAILED,
                    message="No se pudo leer el texto de la placa",
                    plate_number=None,
                    detection=format_detection_result(
                        detection['bbox'],
                        detection['confidence']
                    ),
                    ocr=None,
                    processing_time_ms=processing_time
                )

            # PASO 4: Validación y normalización
            logger.info("PASO 4: Validación y normalización")
            validation_result = self._validate_and_normalize(ocr_result)

            # PASO 5: Construcción de respuesta
            end_time = datetime.now()
            processing_time = format_processing_time(start_time, end_time)

            # Determinar status final
            status, message = get_status_and_message(
                has_detection=True,
                has_ocr=True,
                ocr_confidence=ocr_result['confidence'],
                has_valid_format=validation_result['is_valid'],
                min_confidence=request.ocr_confidence
            )

            # Formatear resultados
            detection_formatted = format_detection_result(
                detection['bbox'],
                detection['confidence']
            )

            ocr_formatted = format_ocr_result(
                ocr_result['text'],
                ocr_result['confidence']
            )

            response = RecognitionResponse(
                status=status,
                message=message,
                plate_number=validation_result['normalized_plate'],
                detection=detection_formatted,
                ocr=ocr_formatted,
                processing_time_ms=processing_time
            )

            logger.info(
                f"=== Reconocimiento completado ===\n"
                f"Placa: {validation_result['normalized_plate']}\n"
                f"Status: {status}\n"
                f"Tiempo: {processing_time}ms"
            )

            return response

        except Exception as e:
            logger.error(f"Error en reconocimiento: {str(e)}")

            end_time = datetime.now()
            processing_time = format_processing_time(start_time, end_time)

            return RecognitionResponse(
                status="ERROR",
                message=f"Error en el proceso de reconocimiento: {str(e)}",
                plate_number=None,
                detection=None,
                ocr=None,
                processing_time_ms=processing_time
            )

    def _detect_plate(
            self,
            image: np.ndarray,
            confidence_threshold: Optional[float] = None
    ) -> Optional[Dict]:
        """
        Detecta placa usando YOLO26

        Args:
            image: Imagen completa
            confidence_threshold: Umbral de confianza

        Returns:
            Mejor detección o None
        """
        detection = self.yolo_detector.get_best_detection(image, confidence_threshold)

        if detection:
            logger.info(
                f"✓ Placa detectada con confianza: {detection['confidence']:.3f}"
            )
        else:
            logger.warning("✗ No se detectó ninguna placa")

        return detection

    def _read_plate_text(
            self,
            plate_image: np.ndarray,
            min_confidence: Optional[float] = None
    ) -> Optional[Dict]:
        """
        Lee texto de la placa usando EasyOCR

        Args:
            plate_image: Imagen recortada de la placa
            min_confidence: Confianza mínima

        Returns:
            Resultado OCR o None
        """
        # Intentar lectura con múltiples intentos
        ocr_result = self.ocr_reader.read_multiple_attempts(
            plate_image,
            min_confidence,
            max_attempts=2
        )

        if ocr_result:
            logger.info(
                f"✓ Texto OCR leído: '{ocr_result['text']}' "
                f"(confianza: {ocr_result['confidence']:.3f})"
            )
        else:
            logger.warning("✗ No se pudo leer el texto de la placa")

        return ocr_result

    def _validate_and_normalize(self, ocr_result: Dict) -> Dict:
        """
        Valida y normaliza el texto de la placa

        Args:
            ocr_result: Resultado del OCR

        Returns:
            Diccionario con validación y normalización
        """
        text = ocr_result['text']
        confidence = ocr_result['confidence']

        # Normalizar formato (ABC123 -> ABC-123)
        normalized = normalize_plate_format(text)

        # Validar formato
        is_valid = validate_plate_format(normalized) if normalized else False

        # Calcular score de calidad
        quality_score = calculate_ocr_quality_score(
            text,
            confidence,
            is_valid
        )

        result = {
            "original_text": text,
            "normalized_plate": normalized,
            "is_valid": is_valid,
            "quality_score": quality_score
        }

        logger.info(
            f"Validación - Original: '{text}', "
            f"Normalizada: '{normalized}', "
            f"Válida: {is_valid}, "
            f"Score: {quality_score:.2f}"
        )

        return result

    def get_service_status(self) -> Dict:
        """
        Obtiene el estado del servicio y los modelos

        Returns:
            Diccionario con estado completo
        """
        return {
            "service": "PlateRecognitionService",
            "status": "OK",
            "yolo_model": self.yolo_detector.get_model_info(),
            "ocr_model": self.ocr_reader.get_model_info()
        }