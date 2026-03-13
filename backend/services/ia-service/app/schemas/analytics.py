"""
Schemas de analíticas y métricas para el servicio IA
"""
from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime
from app.schemas.base import BaseResponse


class RecognitionMetrics(BaseModel):
    """Métricas de reconocimiento de placas"""
    total_recognitions: int = Field(..., ge=0, description="Total de reconocimientos procesados")
    successful_recognitions: int = Field(..., ge=0, description="Reconocimientos exitosos")
    failed_recognitions: int = Field(..., ge=0, description="Reconocimientos fallidos")
    success_rate: float = Field(..., ge=0.0, le=100.0, description="Tasa de éxito (%)")
    average_confidence: float = Field(..., ge=0.0, le=1.0, description="Confianza promedio")
    average_processing_time_ms: int = Field(..., ge=0, description="Tiempo promedio de procesamiento (ms)")

    class Config:
        json_schema_extra = {
            "example": {
                "total_recognitions": 1500,
                "successful_recognitions": 1428,
                "failed_recognitions": 72,
                "success_rate": 95.2,
                "average_confidence": 0.89,
                "average_processing_time_ms": 245
            }
        }


class YOLOMetrics(BaseModel):
    """Métricas específicas de YOLO"""
    total_detections: int = Field(..., ge=0, description="Total de detecciones YOLO")
    successful_detections: int = Field(..., ge=0, description="Detecciones exitosas")
    average_yolo_confidence: float = Field(..., ge=0.0, le=1.0, description="Confianza promedio YOLO")
    average_yolo_time_ms: int = Field(..., ge=0, description="Tiempo promedio YOLO (ms)")

    class Config:
        json_schema_extra = {
            "example": {
                "total_detections": 1500,
                "successful_detections": 1450,
                "average_yolo_confidence": 0.92,
                "average_yolo_time_ms": 85
            }
        }


class OCRMetrics(BaseModel):
    """Métricas específicas de OCR"""
    total_ocr_attempts: int = Field(..., ge=0, description="Total de intentos OCR")
    successful_ocr: int = Field(..., ge=0, description="OCR exitosos")
    average_ocr_confidence: float = Field(..., ge=0.0, le=1.0, description="Confianza promedio OCR")
    average_ocr_time_ms: int = Field(..., ge=0, description="Tiempo promedio OCR (ms)")

    class Config:
        json_schema_extra = {
            "example": {
                "total_ocr_attempts": 1450,
                "successful_ocr": 1428,
                "average_ocr_confidence": 0.88,
                "average_ocr_time_ms": 160
            }
        }


class MetricsResponse(BaseResponse):
    """Respuesta completa de métricas"""
    period_start: datetime = Field(..., description="Inicio del período de métricas")
    period_end: datetime = Field(..., description="Fin del período de métricas")
    recognition_metrics: RecognitionMetrics = Field(..., description="Métricas generales")
    yolo_metrics: YOLOMetrics = Field(..., description="Métricas de YOLO")
    ocr_metrics: OCRMetrics = Field(..., description="Métricas de OCR")

    class Config:
        json_schema_extra = {
            "example": {
                "status": "SUCCESS",
                "message": "Métricas obtenidas exitosamente",
                "timestamp": "2026-03-07T10:30:00",
                "period_start": "2026-03-01T00:00:00",
                "period_end": "2026-03-07T23:59:59",
                "recognition_metrics": {
                    "total_recognitions": 1500,
                    "successful_recognitions": 1428,
                    "failed_recognitions": 72,
                    "success_rate": 95.2,
                    "average_confidence": 0.89,
                    "average_processing_time_ms": 245
                },
                "yolo_metrics": {
                    "total_detections": 1500,
                    "successful_detections": 1450,
                    "average_yolo_confidence": 0.92,
                    "average_yolo_time_ms": 85
                },
                "ocr_metrics": {
                    "total_ocr_attempts": 1450,
                    "successful_ocr": 1428,
                    "average_ocr_confidence": 0.88,
                    "average_ocr_time_ms": 160
                }
            }
        }