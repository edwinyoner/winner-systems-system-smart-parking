"""
Schemas de health check para el servicio IA
"""
from pydantic import BaseModel, Field
from typing import Optional, Dict
from datetime import datetime


class ModelStatus(BaseModel):
    """Estado de un modelo de Deep Learning"""
    loaded: bool = Field(..., description="Modelo cargado en memoria")
    model_path: str = Field(..., description="Ruta del modelo")
    model_size_mb: Optional[float] = Field(default=None, description="Tamaño del modelo en MB")
    last_loaded: Optional[datetime] = Field(default=None, description="Última vez que se cargó")

    class Config:
        json_schema_extra = {
            "example": {
                "loaded": True,
                "model_path": "app/ml/dl/models/yolo26n.pt",
                "model_size_mb": 6.5,
                "last_loaded": "2026-03-07T08:00:00"
            }
        }


class DatabaseStatus(BaseModel):
    """Estado de la conexión a MongoDB"""
    connected: bool = Field(..., description="Conexión activa")
    database_name: str = Field(..., description="Nombre de la base de datos")
    ping_time_ms: Optional[int] = Field(default=None, description="Tiempo de ping (ms)")

    class Config:
        json_schema_extra = {
            "example": {
                "connected": True,
                "database_name": "smart_parking_ia",
                "ping_time_ms": 12
            }
        }


class HealthResponse(BaseModel):
    """Respuesta de health check completo"""
    status: str = Field(..., description="Estado general del servicio")
    service: str = Field(default="ia-service", description="Nombre del servicio")
    version: str = Field(default="1.0.0", description="Versión del servicio")
    timestamp: datetime = Field(default_factory=datetime.now, description="Timestamp del health check")
    uptime_seconds: Optional[int] = Field(default=None, description="Tiempo de ejecución (segundos)")
    yolo_model: Optional[ModelStatus] = Field(default=None, description="Estado del modelo YOLO")
    ocr_model: Optional[ModelStatus] = Field(default=None, description="Estado del modelo OCR")
    database: Optional[DatabaseStatus] = Field(default=None, description="Estado de MongoDB")

    class Config:
        json_schema_extra = {
            "example": {
                "status": "OK",
                "service": "ia-service",
                "version": "1.0.0",
                "timestamp": "2026-03-07T10:30:00",
                "uptime_seconds": 3600,
                "yolo_model": {
                    "loaded": True,
                    "model_path": "app/ml/dl/models/yolo26n.pt",
                    "model_size_mb": 6.5,
                    "last_loaded": "2026-03-07T08:00:00"
                },
                "ocr_model": {
                    "loaded": True,
                    "model_path": "EasyOCR (en memoria)",
                    "model_size_mb": 12.3,
                    "last_loaded": "2026-03-07T08:00:15"
                },
                "database": {
                    "connected": True,
                    "database_name": "smart_parking_ia",
                    "ping_time_ms": 12
                }
            }
        }