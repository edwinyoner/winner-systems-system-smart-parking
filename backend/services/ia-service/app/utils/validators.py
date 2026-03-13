"""
Validadores para el servicio IA
"""
import re
from typing import Optional
from app.core.constants import (
    PERU_PLATE_PATTERN,
    PERU_OLD_PLATE_PATTERN,
    PERU_NEW_PLATE_PATTERN,
    OCR_ALLOWED_CHARS,
    OCR_MIN_TEXT_LENGTH,
    OCR_MAX_TEXT_LENGTH
)
from app.core.logging import logger


def validate_plate_format(plate_text: str) -> bool:
    """
    Valida que el texto de la placa cumpla con el formato peruano

    Formatos válidos:
    - Antiguo: XX-9999 (ejemplo: AB-1234)
    - Nuevo: XXX-999 (ejemplo: ABC-123)
    - Sin guión: XXXXXX o XXXXXXX

    Args:
        plate_text: Texto de la placa a validar

    Returns:
        True si el formato es válido
    """
    if not plate_text:
        return False

    # Limpiar texto (quitar espacios)
    plate_clean = plate_text.strip().upper()

    # Validar con patrones
    patterns = [PERU_PLATE_PATTERN, PERU_OLD_PLATE_PATTERN, PERU_NEW_PLATE_PATTERN]

    for pattern in patterns:
        if re.match(pattern, plate_clean):
            logger.debug(f"Placa '{plate_text}' válida con patrón {pattern}")
            return True

    logger.warning(f"Placa '{plate_text}' no cumple ningún formato válido")
    return False


def clean_plate_text(ocr_text: str) -> str:
    """
    Limpia el texto OCR para obtener solo caracteres válidos de placa

    Args:
        ocr_text: Texto crudo del OCR

    Returns:
        Texto limpio con solo caracteres válidos
    """
    # Convertir a mayúsculas
    text = ocr_text.upper()

    # Remover espacios
    text = text.replace(" ", "")

    # Mantener solo caracteres permitidos
    cleaned = ''.join(c for c in text if c in OCR_ALLOWED_CHARS)

    logger.debug(f"Texto OCR limpio: '{ocr_text}' -> '{cleaned}'")

    return cleaned


def validate_ocr_length(text: str) -> bool:
    """
    Valida que la longitud del texto OCR esté en el rango esperado

    Args:
        text: Texto a validar

    Returns:
        True si la longitud es válida (6-7 caracteres)
    """
    length = len(text)
    is_valid = OCR_MIN_TEXT_LENGTH <= length <= OCR_MAX_TEXT_LENGTH

    if not is_valid:
        logger.warning(f"Longitud de texto '{text}' ({length}) fuera de rango válido")

    return is_valid


def validate_confidence(confidence: float, min_threshold: float = 0.0) -> bool:
    """
    Valida que la confianza esté por encima del umbral mínimo

    Args:
        confidence: Valor de confianza (0.0 - 1.0)
        min_threshold: Umbral mínimo requerido

    Returns:
        True si la confianza es suficiente
    """
    is_valid = confidence >= min_threshold

    if not is_valid:
        logger.warning(f"Confianza {confidence:.2f} por debajo del umbral {min_threshold:.2f}")

    return is_valid


def normalize_plate_format(plate_text: str) -> Optional[str]:
    """
    Normaliza el formato de la placa al estándar XXX-999

    Args:
        plate_text: Texto de la placa

    Returns:
        Placa normalizada o None si no es válida
    """
    # Limpiar texto
    cleaned = clean_plate_text(plate_text)

    # Validar longitud
    if not validate_ocr_length(cleaned):
        return None

    # Si tiene 6 caracteres: ABC123 -> ABC-123
    if len(cleaned) == 6:
        return f"{cleaned[:3]}-{cleaned[3:]}"

    # Si tiene 7 caracteres y no tiene guión: ABC-123 o ABCD123
    if len(cleaned) == 7:
        # Verificar si ya tiene formato correcto
        if validate_plate_format(cleaned):
            return cleaned
        # Si no, intentar formato XXX-999
        return f"{cleaned[:3]}-{cleaned[3:6]}"

    # Si ya tiene formato válido, retornar tal cual
    if validate_plate_format(cleaned):
        return cleaned

    logger.warning(f"No se pudo normalizar placa: '{plate_text}'")
    return None


def calculate_ocr_quality_score(
        text: str,
        confidence: float,
        has_valid_format: bool
) -> float:
    """
    Calcula un score de calidad del OCR basado en múltiples factores

    Args:
        text: Texto reconocido
        confidence: Confianza del OCR
        has_valid_format: Si cumple formato de placa válido

    Returns:
        Score de calidad (0.0 - 1.0)
    """
    score = 0.0

    # Confianza base (50% del score)
    score += confidence * 0.5

    # Formato válido (30% del score)
    if has_valid_format:
        score += 0.3

    # Longitud correcta (20% del score)
    if validate_ocr_length(text):
        score += 0.2

    logger.debug(f"Score de calidad OCR para '{text}': {score:.2f}")

    return min(score, 1.0)