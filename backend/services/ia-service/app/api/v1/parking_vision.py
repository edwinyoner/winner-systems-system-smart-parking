# ia-service/app/api/v1/parking_vision.py

"""
Endpoint REST para detección de visión de estacionamiento (Parking Vision).

Este endpoint procesa imágenes aéreas de estacionamientos para detectar:
- Ocupación de espacios (YOLO26)
- Placas de vehículos (OCR desde vista aérea)

Flujo:
1. iot-service envía imagen + polígonos
2. ia-service procesa con YOLO + OCR
3. Retorna ocupación por espacio + placas detectadas
"""

import logging
from typing import Dict, Any

from fastapi import APIRouter, HTTPException, status
from fastapi.responses import JSONResponse

from app.services.parking_vision_service import (
    ParkingVisionService,
    ParkingVisionRequest,
    ParkingVisionResponse,
    get_parking_vision_service
)

logger = logging.getLogger(__name__)

# ============================================================================
# ROUTER CONFIGURATION
# ============================================================================

router = APIRouter(
    prefix="/parking-vision",
    tags=["Parking Vision"],
    responses={
        404: {"description": "Not found"},
        500: {"description": "Internal server error"}
    }
)


# ============================================================================
# ENDPOINTS
# ============================================================================

@router.post(
    "",
    response_model=ParkingVisionResponse,
    status_code=status.HTTP_200_OK,
    summary="Detectar ocupación de estacionamiento desde vista aérea",
    description="""
    Procesa una imagen aérea de estacionamiento para detectar:
    - Espacios ocupados vs disponibles (usando YOLO26)
    - Placas de vehículos en vista aérea (usando OCR)

    **Flujo:**
    1. Recibe imagen desde S3 + polígonos de espacios
    2. Detecta vehículos en cada espacio (YOLO26)
    3. Intenta leer placas desde vista aérea (OCR - menor precisión que frontal)
    4. Retorna ocupación completa + placas detectadas

    **Casos de uso:**
    - ESCENARIO_01: Parkings sin puertas (Jr. Simón Bolívar)
    - Monitoreo continuo desde ESP32-CAM aérea
    - Actualización masiva de ocupación (bulk update)

    **Notas:**
    - Precisión OCR aérea: ~70-80% (vs 96% frontal)
    - Requiere polígonos pre-configurados (generados con dron + ParkingPtsSelection)
    - Soporta múltiples parkings (polígonos dinámicos)
    """,
    responses={
        200: {
            "description": "Detección exitosa",
            "content": {
                "application/json": {
                    "example": {
                        "parkingId": 1,
                        "snapshotId": "snapshot-1-20260312-110530-123",
                        "imageUrl": "s3://smart-parking/parking-1/snapshot.jpg",
                        "annotatedImageUrl": "s3://smart-parking/parking-1/annotated/20260312-110530.jpg",
                        "totalSpaces": 50,
                        "occupiedCount": 32,
                        "availableCount": 18,
                        "platesDetectedCount": 28,
                        "detections": [
                            {
                                "spaceId": "A-001",
                                "occupied": True,
                                "vehicleDetected": True,
                                "vehicleType": "car",
                                "vehicleConfidence": 0.92,
                                "plateNumber": "ABC-123",
                                "plateConfidence": 0.85,
                                "plateDetected": True
                            },
                            {
                                "spaceId": "A-002",
                                "occupied": False,
                                "vehicleDetected": False,
                                "vehicleType": None,
                                "vehicleConfidence": 0.0,
                                "plateNumber": None,
                                "plateConfidence": 0.0,
                                "plateDetected": False
                            }
                        ],
                        "processedAt": "2026-03-12T11:05:30.123456",
                        "processingTimeMs": 1250.45
                    }
                }
            }
        },
        400: {
            "description": "Request inválido",
            "content": {
                "application/json": {
                    "example": {
                        "detail": "Debe proporcionar al menos un polígono"
                    }
                }
            }
        },
        404: {
            "description": "Imagen no encontrada en S3",
            "content": {
                "application/json": {
                    "example": {
                        "detail": "Imagen no encontrada: s3://bucket/invalid.jpg"
                    }
                }
            }
        },
        500: {
            "description": "Error interno del servidor",
            "content": {
                "application/json": {
                    "example": {
                        "detail": "Error procesando imagen: [detalles]"
                    }
                }
            }
        }
    }
)
async def detect_parking_occupancy(
        request: ParkingVisionRequest
) -> ParkingVisionResponse:
    """
    Detecta ocupación de espacios de estacionamiento desde vista aérea.

    Args:
        request: ParkingVisionRequest con imagen, polígonos y configuración

    Returns:
        ParkingVisionResponse con detecciones por espacio

    Raises:
        HTTPException: Si hay errores durante el procesamiento
    """
    try:
        logger.info(
            f"📸 POST /v1/parking-vision - parking_id={request.parking_id}, "
            f"espacios={len(request.polygons)}, "
            f"detect_plates={request.detect_plates}"
        )

        # Obtener servicio (singleton)
        service: ParkingVisionService = get_parking_vision_service()

        # Procesar imagen
        result = await service.process_parking_vision(request)

        logger.info(
            f"Parking vision completado - parking_id={request.parking_id}, "
            f"ocupados={result.occupied_count}/{result.total_spaces}, "
            f"placas={result.plates_detected_count}, "
            f"tiempo={result.processing_time_ms}ms"
        )

        return result

    except ValueError as e:
        # Errores de validación (400 Bad Request)
        logger.warning(f"Request inválido: {e}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )

    except FileNotFoundError as e:
        # Imagen no encontrada en S3 (404 Not Found)
        logger.warning(f"Imagen no encontrada: {e}")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Imagen no encontrada: {str(e)}"
        )

    except Exception as e:
        # Error interno (500 Internal Server Error)
        logger.error(f"Error procesando parking vision: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error interno procesando imagen: {str(e)}"
        )


@router.get(
    "/health",
    status_code=status.HTTP_200_OK,
    summary="Health check del módulo parking-vision",
    description="Verifica que el módulo de parking vision esté operativo",
    responses={
        200: {
            "description": "Módulo operativo",
            "content": {
                "application/json": {
                    "example": {
                        "status": "healthy",
                        "module": "parking-vision",
                        "yolo_model_loaded": True,
                        "ocr_available": True
                    }
                }
            }
        }
    }
)
async def parking_vision_health() -> Dict[str, Any]:
    """
    Health check específico para parking vision.

    Verifica:
    - Servicio inicializado correctamente
    - Modelo YOLO cargado
    - OCR disponible

    Returns:
        Estado del módulo
    """
    try:
        service = get_parking_vision_service()

        # Verificar que el detector de ocupación esté inicializado
        yolo_loaded = service.occupancy_detector is not None

        # Verificar que el servicio de placas esté disponible
        ocr_available = service.plate_service is not None

        return {
            "status": "healthy" if (yolo_loaded and ocr_available) else "degraded",
            "module": "parking-vision",
            "yolo_model_loaded": yolo_loaded,
            "ocr_available": ocr_available
        }

    except Exception as e:
        logger.error(f"Error en health check parking-vision: {e}")
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail=f"Parking vision no disponible: {str(e)}"
        )


@router.post(
    "/validate-polygons",
    status_code=status.HTTP_200_OK,
    summary="Validar polígonos de estacionamiento",
    description="""
    Valida que un conjunto de polígonos sea correcto antes de usarlo en detección.

    Útil para:
    - Verificar JSON generado por ParkingPtsSelection
    - Validar polígonos antes de guardar en base de datos
    - Testing de configuración
    """,
    responses={
        200: {
            "description": "Polígonos válidos",
            "content": {
                "application/json": {
                    "example": {
                        "valid": True,
                        "total_spaces": 50,
                        "space_ids": ["A-001", "A-002", "..."],
                        "message": "Polígonos válidos"
                    }
                }
            }
        },
        400: {
            "description": "Polígonos inválidos",
            "content": {
                "application/json": {
                    "example": {
                        "detail": "Polígono 'A-001' inválido: debe tener al menos 3 puntos"
                    }
                }
            }
        }
    }
)
async def validate_polygons(
        polygons: Dict[str, list[list[float]]]
) -> Dict[str, Any]:
    """
    Valida un conjunto de polígonos.

    Args:
        polygons: Diccionario de polígonos {space_id: [[x,y], ...]}

    Returns:
        Resultado de validación

    Raises:
        HTTPException: Si los polígonos son inválidos
    """
    try:
        from app.utils.polygon_utils import load_polygons_from_dict

        # Intentar cargar y validar polígonos
        polygon_set = load_polygons_from_dict(polygons)

        return {
            "valid": True,
            "total_spaces": polygon_set.get_total_spaces(),
            "space_ids": list(polygons.keys()),
            "message": "Polígonos válidos"
        }

    except Exception as e:
        logger.warning(f"Polígonos inválidos: {e}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )