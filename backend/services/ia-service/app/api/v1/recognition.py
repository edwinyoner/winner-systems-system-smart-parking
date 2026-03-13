"""
Endpoints REST para reconocimiento de placas vehiculares
API Version 1
"""
from fastapi import APIRouter, UploadFile, File, HTTPException, Form
from typing import Optional
import numpy as np

from app.core.logging import logger
from app.core.constants import MSG_INVALID_IMAGE, MSG_IMAGE_TOO_LARGE
from app.core.exceptions import (
    ImageProcessingException,
    YOLODetectionException,
    OCRException
)
from app.schemas.recognition import RecognitionResponse, RecognitionRequest
from app.services.plate_recognition_service import PlateRecognitionService
from app.utils.image_utils import (
    read_image_from_bytes,
    validate_image_format,
    validate_image_size
)

# Crear router con versionado y recurso en plural
router = APIRouter(
    prefix="/recognitions",
    tags=["Plate Recognition"]
)

# Instancia global del servicio (se carga una sola vez)
# TODO: Migrar a Dependency Injection con Depends() en futuras versiones
plate_service = PlateRecognitionService()


@router.post(
    "",
    response_model=RecognitionResponse,
    summary="Reconocer placa vehicular",
    description="""
    Reconoce una placa vehicular en una imagen usando Deep Learning.

    Pipeline:
    1. Detección de placa con YOLO26 (CNN)
    2. Lectura de texto con EasyOCR (CNN + RNN)
    3. Validación y normalización de formato

    Formatos de imagen soportados: JPG, JPEG, PNG
    Tamaño máximo: 10 MB

    **Endpoint:** POST /api/v1/recognitions
    """
)
async def recognize_plate(
        image: UploadFile = File(..., description="Imagen con placa vehicular"),
        yolo_confidence: Optional[float] = Form(
            default=0.25,
            ge=0.0,
            le=1.0,
            description="Umbral de confianza para YOLO (0.0 - 1.0)"
        ),
        ocr_confidence: Optional[float] = Form(
            default=0.5,
            ge=0.0,
            le=1.0,
            description="Umbral de confianza para OCR (0.0 - 1.0)"
        )
) -> RecognitionResponse:
    """
    Endpoint principal para reconocer placas vehiculares

    Args:
        image: Archivo de imagen (JPG, PNG)
        yolo_confidence: Confianza mínima para detección YOLO
        ocr_confidence: Confianza mínima para lectura OCR

    Returns:
        RecognitionResponse con placa reconocida y métricas

    Raises:
        HTTPException 400: Si la imagen es inválida
        HTTPException 413: Si la imagen es demasiado grande
        HTTPException 500: Si ocurre un error en el procesamiento
    """
    logger.info(f"Nueva petición de reconocimiento - archivo: {image.filename}")

    try:
        # Validar formato de archivo
        if not validate_image_format(image.filename):
            logger.warning(f"Formato de imagen no válido: {image.filename}")
            raise HTTPException(
                status_code=400,
                detail=MSG_INVALID_IMAGE
            )

        # Leer bytes de la imagen
        image_bytes = await image.read()

        # Validar tamaño
        if not validate_image_size(image_bytes):
            size_mb = len(image_bytes) / (1024 * 1024)
            logger.warning(f"Imagen demasiado grande: {size_mb:.2f} MB")
            raise HTTPException(
                status_code=413,
                detail=MSG_IMAGE_TOO_LARGE
            )

        # Convertir a formato OpenCV
        image_np = read_image_from_bytes(image_bytes)

        logger.info(f"Imagen cargada: {image_np.shape}")

        # Crear request con parámetros
        request = RecognitionRequest(
            yolo_confidence=yolo_confidence,
            ocr_confidence=ocr_confidence
        )

        # Ejecutar reconocimiento
        result = plate_service.recognize_plate(image_np, request)

        logger.info(
            f"Reconocimiento completado - Status: {result.status}, "
            f"Placa: {result.plate_number}, "
            f"Tiempo: {result.processing_time_ms}ms"
        )

        return result

    except ImageProcessingException as e:
        logger.error(f"Error procesando imagen: {e.message}")
        raise HTTPException(status_code=400, detail=e.message)

    except (YOLODetectionException, OCRException) as e:
        logger.error(f"Error en modelos de Deep Learning: {e.message}")
        raise HTTPException(status_code=500, detail=e.message)

    except HTTPException:
        # Re-lanzar HTTPExceptions sin modificar
        raise

    except Exception as e:
        logger.error(f"Error inesperado en reconocimiento: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail=f"Error interno del servidor: {str(e)}"
        )


@router.get(
    "/status",
    summary="Estado del servicio de reconocimiento",
    description="""
    Obtiene el estado de los modelos YOLO26 y EasyOCR.

    **Endpoint:** GET /api/v1/recognitions/status
    """
)
async def get_recognition_status():
    """
    Obtiene el estado del servicio y los modelos de Deep Learning

    Returns:
        Estado de YOLO26 y EasyOCR (cargados, rutas, tamaños)
    """
    try:
        status = plate_service.get_service_status()
        logger.debug("Estado del servicio solicitado")
        return status

    except Exception as e:
        logger.error(f"Error obteniendo estado: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail=f"Error al obtener estado: {str(e)}"
        )