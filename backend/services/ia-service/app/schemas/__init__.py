"""
Schemas (DTOs) para requests y responses
"""
from app.schemas.base import BaseResponse, ErrorResponse, BoundingBox
from app.schemas.recognition import (
    RecognitionRequest,
    RecognitionResponse,
    DetectionResult,
    OCRResult
)
from app.schemas.analytics import (
    RecognitionMetrics,
    YOLOMetrics,
    OCRMetrics,
    MetricsResponse
)
from app.schemas.health import HealthResponse, ModelStatus, DatabaseStatus

__all__ = [
    # Base
    "BaseResponse",
    "ErrorResponse",
    "BoundingBox",
    # Recognition
    "RecognitionRequest",
    "RecognitionResponse",
    "DetectionResult",
    "OCRResult",
    # Analytics
    "RecognitionMetrics",
    "YOLOMetrics",
    "OCRMetrics",
    "MetricsResponse",
    # Health
    "HealthResponse",
    "ModelStatus",
    "DatabaseStatus",
]