# ia-service/app/main.py

"""
IA Service - System Smart Parking
Entry point for FastAPI application
"""
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import uvicorn

from app.core.config import settings
from app.core.logging import logger
from app.api.v1 import recognition, parking_vision


# ====================================
# Lifespan Event Handler (Startup/Shutdown)
# ====================================
@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Gestiona eventos de ciclo de vida de la aplicación
    - Startup: Se ejecuta al iniciar el servicio
    - Shutdown: Se ejecuta al cerrar el servicio
    """
    # ========== STARTUP ==========
    logger.info("=" * 60)
    logger.info(f"Iniciando {settings.APP_NAME} v{settings.APP_VERSION}")
    logger.info(f"Puerto: {settings.APP_PORT}")
    logger.info(f"Debug: {settings.DEBUG}")
    logger.info(f"Documentación: http://localhost:{settings.APP_PORT}/api/docs")
    logger.info("=" * 60)

    # Los modelos YOLO y OCR se cargan lazy cuando se crea PlateRecognitionService
    # en el primer request a /api/v1/recognitions
    logger.info("Servicio listo para recibir peticiones")
    logger.info("📍 Endpoints disponibles:")
    logger.info("   - POST /api/v1/recognitions (reconocimiento de placas)")
    logger.info("   - POST /api/v1/parking-vision (detección de ocupación)")  # ← NUEVO
    logger.info("   - GET  /api/v1/recognitions/status")
    logger.info("   - GET  /api/v1/parking-vision/health")  # ← NUEVO

    yield  # ← Aquí corre la aplicación

    # ========== SHUTDOWN ==========
    logger.info("Cerrando IA Service...")
    logger.info("Servicio detenido correctamente")


# ====================================
# App metadata con lifespan
# ====================================
app = FastAPI(
    title=settings.APP_NAME,
    description="AI Service for License Plate Recognition and Parking Vision using Deep Learning (YOLO26 + EasyOCR)",  # ← Actualizado
    version=settings.APP_VERSION,
    docs_url="/api/docs",
    redoc_url="/api/redoc",
    openapi_url="/api/openapi.json",
    lifespan=lifespan
)

# CORS configuration
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ====================================
# Registrar routers de API v1
# ====================================
app.include_router(
    recognition.router,
    prefix="/api/v1",
    tags=["Recognition"]  # ← Tag específico
)

app.include_router(
    parking_vision.router,  # ← NUEVO ROUTER
    prefix="/api/v1",
    tags=["Parking Vision"]
)

logger.info("Routers de API v1 registrados")


# ====================================
# Endpoints raíz
# ====================================
@app.get("/")
async def root():
    """
    Endpoint raíz con información del servicio
    """
    return {
        "service": settings.APP_NAME,
        "version": settings.APP_VERSION,
        "status": "running",
        "docs": "/api/docs",
        "endpoints": {
            "plate_recognition": "/api/v1/recognitions",
            "parking_vision": "/api/v1/parking-vision",  # ← NUEVO
            "recognition_status": "/api/v1/recognitions/status",
            "parking_vision_health": "/api/v1/parking-vision/health",  # ← NUEVO
            "health": "/api/health"
        }
    }


@app.get("/api/health")
async def health_check():
    """
    Health check básico del servicio

    Para health check detallado (con estado de modelos YOLO/OCR):
    - GET /api/v1/recognitions/status (plate recognition)
    - GET /api/v1/parking-vision/health (parking vision)
    """
    return {
        "status": "OK",
        "service": "ia-service",
        "version": settings.APP_VERSION,
        "port": settings.APP_PORT,
        "modules": {
            "plate_recognition": "available",
            "parking_vision": "available"  # ← NUEVO
        }
    }


# ====================================
# Entry point para ejecución directa
# ====================================
if __name__ == "__main__":
    uvicorn.run(
        "app.main:app",
        host=settings.APP_HOST,
        port=settings.APP_PORT,
        reload=settings.DEBUG
    )