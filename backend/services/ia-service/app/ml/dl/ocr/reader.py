"""
Lector OCR para reconocimiento de texto en placas vehiculares
Implementación de CNN + RNN para reconocimiento óptico de caracteres
"""
import easyocr
import numpy as np
from typing import Optional, List, Dict, Tuple
from datetime import datetime

from app.core.config import settings
from app.core.logging import logger
from app.core.exceptions import ModelLoadException, OCRException
from app.utils.validators import clean_plate_text, validate_ocr_length
from app.utils.formatters import sanitize_plate_text


class EasyOCRReader:
    """
    Lector de texto para placas vehiculares usando EasyOCR

    EasyOCR combina dos arquitecturas de Deep Learning:
    - CNN (Convolutional Neural Network): Extrae características visuales
    - RNN (Recurrent Neural Network): Procesa secuencias de texto

    Arquitectura:
    - Feature Extractor: VGG/ResNet (CNN)
    - Sequence Model: LSTM/BiLSTM (RNN)
    - Attention Mechanism: Transformer
    """

    def __init__(self):
        """Inicializa el lector OCR"""
        self.reader: Optional[easyocr.Reader] = None
        self.languages: List[str] = settings.OCR_LANGUAGES
        self.gpu: bool = settings.OCR_GPU
        self.min_confidence: float = settings.OCR_MIN_CONFIDENCE
        self.is_loaded: bool = False
        self.load_time: Optional[datetime] = None

        logger.info(f"Inicializando EasyOCRReader con idiomas: {self.languages}")

    def load_model(self) -> None:
        """
        Carga el modelo EasyOCR en memoria

        EasyOCR descarga automáticamente los modelos necesarios en la primera ejecución.
        Los modelos se guardan en ~/.EasyOCR/model/

        Raises:
            ModelLoadException: Si no se puede cargar el modelo
        """
        try:
            logger.info("Cargando modelo EasyOCR...")
            logger.info(f"Idiomas: {self.languages}, GPU: {self.gpu}")

            # Crear reader de EasyOCR
            # Nota: La primera vez descarga modelos (~100MB)
            self.reader = easyocr.Reader(
                lang_list=self.languages,
                gpu=self.gpu,
                verbose=False
            )

            self.is_loaded = True
            self.load_time = datetime.now()

            logger.info("Modelo EasyOCR cargado exitosamente")

        except Exception as e:
            logger.error(f"Error al cargar modelo OCR: {str(e)}")
            raise ModelLoadException(
                message="No se pudo cargar el modelo OCR",
                details={"error": str(e), "languages": self.languages}
            )

    def read_text(
            self,
            image: np.ndarray,
            min_confidence: Optional[float] = None
    ) -> List[Dict]:
        """
        Lee texto de una imagen usando OCR

        Args:
            image: Imagen en formato numpy array (BGR o RGB)
            min_confidence: Umbral mínimo de confianza personalizado

        Returns:
            Lista de resultados OCR, cada uno con:
            - text: Texto reconocido
            - confidence: Confianza (0.0 - 1.0)
            - bbox: Coordenadas del texto

        Raises:
            OCRException: Si la lectura OCR falla
        """
        if not self.is_loaded:
            logger.info("Modelo OCR no cargado, cargando automáticamente...")
            self.load_model()

        try:
            # Usar umbral personalizado o el configurado
            conf_threshold = min_confidence or self.min_confidence

            logger.debug(f"Ejecutando OCR con confianza mínima: {conf_threshold}")

            # Inferencia OCR
            # EasyOCR retorna: [([[x1,y1], [x2,y2], [x3,y3], [x4,y4]], text, confidence), ...]
            results = self.reader.readtext(
                image,
                paragraph=settings.OCR_PARAGRAPH,
                detail=1  # Retorna bbox, text y confidence
            )

            # Procesar resultados
            ocr_results = []

            for bbox, text, confidence in results:
                # Filtrar por confianza
                if confidence < conf_threshold:
                    logger.debug(f"OCR descartado por baja confianza: '{text}' ({confidence:.3f})")
                    continue

                # Limpiar texto
                cleaned_text = clean_plate_text(text)

                if not cleaned_text:
                    logger.debug(f"OCR descartado después de limpieza: '{text}'")
                    continue

                result = {
                    "text": cleaned_text,
                    "confidence": float(confidence),
                    "original_text": text,
                    "bbox": bbox
                }

                ocr_results.append(result)

                logger.debug(
                    f"OCR detectado - texto: '{cleaned_text}', "
                    f"confianza: {confidence:.3f}"
                )

            logger.info(f"Total de textos OCR detectados: {len(ocr_results)}")

            return ocr_results

        except Exception as e:
            logger.error(f"Error en lectura OCR: {str(e)}")
            raise OCRException(
                message="Error al leer texto con OCR",
                details={"error": str(e)}
            )

    def read_plate_text(
            self,
            plate_image: np.ndarray,
            min_confidence: Optional[float] = None
    ) -> Optional[Dict]:
        """
        Lee específicamente el texto de una placa vehicular

        Aplica filtros adicionales para validar que sea una placa:
        - Longitud correcta (6-7 caracteres)
        - Solo caracteres alfanuméricos

        Args:
            plate_image: Imagen de la placa recortada
            min_confidence: Umbral mínimo de confianza

        Returns:
            Resultado OCR con mayor confianza o None
        """
        ocr_results = self.read_text(plate_image, min_confidence)

        if not ocr_results:
            logger.info("No se detectó texto en la placa")
            return None

        # Filtrar resultados válidos para placas
        valid_results = []

        for result in ocr_results:
            text = result['text']

            # Validar longitud (placas tienen 6-7 caracteres)
            if not validate_ocr_length(text):
                logger.debug(f"Texto descartado por longitud: '{text}' (len={len(text)})")
                continue

            valid_results.append(result)

        if not valid_results:
            logger.info("No se encontró texto válido de placa")
            return None

        # Ordenar por confianza (mayor a menor)
        valid_results.sort(key=lambda x: x['confidence'], reverse=True)

        best_result = valid_results[0]
        logger.info(
            f"Mejor lectura OCR: '{best_result['text']}' "
            f"(confianza: {best_result['confidence']:.3f})"
        )

        return best_result

    def read_multiple_attempts(
            self,
            plate_image: np.ndarray,
            min_confidence: Optional[float] = None,
            max_attempts: int = 3
    ) -> Optional[Dict]:
        """
        Intenta leer la placa múltiples veces con diferentes configuraciones

        Útil para placas difíciles de leer (baja iluminación, ángulo, etc.)

        Args:
            plate_image: Imagen de la placa
            min_confidence: Umbral mínimo de confianza
            max_attempts: Número máximo de intentos

        Returns:
            Mejor resultado OCR encontrado o None
        """
        best_result = None
        best_confidence = 0.0

        for attempt in range(max_attempts):
            logger.debug(f"Intento OCR {attempt + 1}/{max_attempts}")

            # En intentos posteriores, reducir el umbral de confianza
            conf_threshold = (min_confidence or self.min_confidence) - (attempt * 0.1)
            conf_threshold = max(0.2, conf_threshold)  # Mínimo 0.2

            result = self.read_plate_text(plate_image, conf_threshold)

            if result and result['confidence'] > best_confidence:
                best_result = result
                best_confidence = result['confidence']
                logger.debug(f"Nuevo mejor resultado: '{result['text']}' ({best_confidence:.3f})")

                # Si tenemos alta confianza, no intentar más
                if best_confidence >= 0.9:
                    logger.info("Alta confianza alcanzada, finalizando intentos")
                    break

        if best_result:
            logger.info(
                f"Mejor resultado después de {max_attempts} intentos: "
                f"'{best_result['text']}' (confianza: {best_confidence:.3f})"
            )
        else:
            logger.warning(f"No se pudo leer la placa después de {max_attempts} intentos")

        return best_result

    def get_model_info(self) -> Dict:
        """
        Obtiene información sobre el modelo OCR cargado

        Returns:
            Diccionario con información del modelo
        """
        info = {
            "loaded": self.is_loaded,
            "languages": self.languages,
            "gpu_enabled": self.gpu,
            "min_confidence": self.min_confidence,
            "last_loaded": self.load_time.isoformat() if self.load_time else None,
            "model_path": "EasyOCR (descargado automáticamente)"
        }

        return info