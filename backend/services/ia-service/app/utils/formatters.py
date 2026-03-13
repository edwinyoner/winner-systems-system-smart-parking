"""
Formateadores para respuestas y datos del servicio IA
"""
from datetime import datetime
from typing import Optional, Dict, Any
from app.core.constants import (
    STATUS_SUCCESS,
    STATUS_NO_PLATE_DETECTED,
    STATUS_OCR_FAILED,
    STATUS_LOW_CONFIDENCE,
    STATUS_INVALID_FORMAT,
    STATUS_ERROR,
    MSG_PLATE_RECOGNIZED,
    MSG_NO_PLATE_FOUND,
    MSG_OCR_FAILED,
    MSG_LOW_CONFIDENCE,
    MSG_INVALID_IMAGE
)
from app.schemas.base import BoundingBox
from app.schemas.recognition import DetectionResult, OCRResult


def format_bbox_dict(x: int, y: int, width: int, height: int) -> Dict[str, int]:
    """
    Formatea coordenadas de bounding box a diccionario

    Args:
        x: Coordenada X (top-left)
        y: Coordenada Y (top-left)
        width: Ancho del bbox
        height: Alto del bbox

    Returns:
        Diccionario con formato {x, y, width, height}
    """
    return {
        "x": int(x),
        "y": int(y),
        "width": int(width),
        "height": int(height)
    }


def format_detection_result(
        bbox: Dict[str, int],
        confidence: float
) -> DetectionResult:
    """
    Formatea resultado de detección YOLO

    Args:
        bbox: Bounding box {x, y, width, height}
        confidence: Confianza de la detección

    Returns:
        DetectionResult formateado
    """
    return DetectionResult(
        bbox=BoundingBox(**bbox),
        confidence=round(confidence, 4)
    )


def format_ocr_result(text: str, confidence: float) -> OCRResult:
    """
    Formatea resultado de OCR

    Args:
        text: Texto reconocido
        confidence: Confianza del OCR

    Returns:
        OCRResult formateado
    """
    return OCRResult(
        text=text.upper().strip(),
        confidence=round(confidence, 4)
    )


def get_status_and_message(
        has_detection: bool,
        has_ocr: bool,
        ocr_confidence: float,
        has_valid_format: bool,
        min_confidence: float = 0.5
) -> tuple[str, str]:
    """
    Determina el status y mensaje basado en los resultados del reconocimiento

    Args:
        has_detection: Si se detectó una placa con YOLO
        has_ocr: Si OCR logró leer texto
        ocr_confidence: Confianza del OCR
        has_valid_format: Si el texto cumple formato de placa
        min_confidence: Umbral mínimo de confianza

    Returns:
        Tupla (status, message)
    """
    # Sin detección YOLO
    if not has_detection:
        return STATUS_NO_PLATE_DETECTED, MSG_NO_PLATE_FOUND

    # Sin texto OCR
    if not has_ocr:
        return STATUS_OCR_FAILED, MSG_OCR_FAILED

    # Baja confianza
    if ocr_confidence < min_confidence:
        return STATUS_LOW_CONFIDENCE, MSG_LOW_CONFIDENCE

    # Formato inválido
    if not has_valid_format:
        return STATUS_INVALID_FORMAT, f"Formato de placa no reconocido"

    # Todo OK
    return STATUS_SUCCESS, MSG_PLATE_RECOGNIZED


def format_error_response(error_type: str, error_message: str) -> Dict[str, Any]:
    """
    Formatea respuesta de error

    Args:
        error_type: Tipo de error
        error_message: Mensaje de error

    Returns:
        Diccionario con respuesta de error
    """
    return {
        "status": STATUS_ERROR,
        "message": error_message,
        "error_type": error_type,
        "timestamp": datetime.now().isoformat()
    }


def format_processing_time(start_time: datetime, end_time: datetime) -> int:
    """
    Calcula tiempo de procesamiento en milisegundos

    Args:
        start_time: Tiempo de inicio
        end_time: Tiempo de fin

    Returns:
        Tiempo en milisegundos
    """
    delta = end_time - start_time
    return int(delta.total_seconds() * 1000)


def format_confidence_percentage(confidence: float) -> str:
    """
    Formatea confianza como porcentaje legible

    Args:
        confidence: Valor de confianza (0.0 - 1.0)

    Returns:
        String con formato "95.2%"
    """
    return f"{confidence * 100:.1f}%"


def sanitize_plate_text(text: str) -> str:
    """
    Sanitiza texto de placa para almacenamiento/comparación

    Args:
        text: Texto a sanitizar

    Returns:
        Texto sanitizado (mayúsculas, sin espacios)
    """
    return text.upper().strip().replace(" ", "")


def format_success_response(
        plate_number: str,
        detection_result: DetectionResult,
        ocr_result: OCRResult,
        processing_time_ms: int
) -> Dict[str, Any]:
    """
    Formatea respuesta exitosa completa

    Args:
        plate_number: Número de placa normalizado
        detection_result: Resultado de YOLO
        ocr_result: Resultado de OCR
        processing_time_ms: Tiempo de procesamiento

    Returns:
        Diccionario con respuesta completa
    """
    return {
        "status": STATUS_SUCCESS,
        "message": MSG_PLATE_RECOGNIZED,
        "timestamp": datetime.now().isoformat(),
        "plate_number": plate_number,
        "detection": detection_result.model_dump(),
        "ocr": ocr_result.model_dump(),
        "processing_time_ms": processing_time_ms
    }