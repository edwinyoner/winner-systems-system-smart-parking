"""
Recognition schemas for license plate detection and OCR
"""
from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime
from app.schemas.base import BaseResponse, BoundingBox


class RecognitionRequest(BaseModel):
    """Request parameters for plate recognition"""
    yolo_confidence: Optional[float] = Field(
        default=0.25,
        ge=0.0,
        le=1.0,
        description="YOLO detection confidence threshold"
    )
    ocr_confidence: Optional[float] = Field(
        default=0.5,
        ge=0.0,
        le=1.0,
        description="OCR minimum confidence threshold"
    )

    class Config:
        json_schema_extra = {
            "example": {
                "yolo_confidence": 0.25,
                "ocr_confidence": 0.5
            }
        }


class DetectionResult(BaseModel):
    """YOLO detection result"""
    bbox: BoundingBox = Field(..., description="Bounding box of detected plate")
    confidence: float = Field(..., ge=0.0, le=1.0, description="Detection confidence")

    class Config:
        json_schema_extra = {
            "example": {
                "bbox": {"x": 100, "y": 150, "width": 200, "height": 80},
                "confidence": 0.95
            }
        }


class OCRResult(BaseModel):
    """OCR recognition result"""
    text: str = Field(..., description="Recognized plate text")
    confidence: float = Field(..., ge=0.0, le=1.0, description="OCR confidence")

    class Config:
        json_schema_extra = {
            "example": {
                "text": "ABC-123",
                "confidence": 0.92
            }
        }


class RecognitionResponse(BaseResponse):
    """Complete recognition response"""
    plate_number: Optional[str] = Field(default=None, description="Recognized plate number")
    detection: Optional[DetectionResult] = Field(default=None, description="YOLO detection details")
    ocr: Optional[OCRResult] = Field(default=None, description="OCR recognition details")
    processing_time_ms: int = Field(..., description="Total processing time in milliseconds")

    class Config:
        json_schema_extra = {
            "example": {
                "status": "SUCCESS",
                "message": "Placa vehicular reconocida exitosamente",
                "timestamp": "2026-03-07T10:30:00",
                "plate_number": "ABC-123",
                "detection": {
                    "bbox": {"x": 100, "y": 150, "width": 200, "height": 80},
                    "confidence": 0.95
                },
                "ocr": {
                    "text": "ABC-123",
                    "confidence": 0.92
                },
                "processing_time_ms": 245
            }
        }

"""
Recognition schemas for license plate detection and OCR
"""
from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime
from app.schemas.base import BaseResponse, BoundingBox


class RecognitionRequest(BaseModel):
    """Request parameters for plate recognition"""
    yolo_confidence: Optional[float] = Field(
        default=0.25,
        ge=0.0,
        le=1.0,
        description="YOLO detection confidence threshold"
    )
    ocr_confidence: Optional[float] = Field(
        default=0.5,
        ge=0.0,
        le=1.0,
        description="OCR minimum confidence threshold"
    )

    class Config:
        json_schema_extra = {
            "example": {
                "yolo_confidence": 0.25,
                "ocr_confidence": 0.5
            }
        }


class DetectionResult(BaseModel):
    """YOLO detection result"""
    bbox: BoundingBox = Field(..., description="Bounding box of detected plate")
    confidence: float = Field(..., ge=0.0, le=1.0, description="Detection confidence")

    class Config:
        json_schema_extra = {
            "example": {
                "bbox": {"x": 100, "y": 150, "width": 200, "height": 80},
                "confidence": 0.95
            }
        }


class OCRResult(BaseModel):
    """OCR recognition result"""
    text: str = Field(..., description="Recognized plate text")
    confidence: float = Field(..., ge=0.0, le=1.0, description="OCR confidence")

    class Config:
        json_schema_extra = {
            "example": {
                "text": "ABC-123",
                "confidence": 0.92
            }
        }


class RecognitionResponse(BaseResponse):
    """Complete recognition response"""
    plate_number: Optional[str] = Field(default=None, description="Recognized plate number")
    detection: Optional[DetectionResult] = Field(default=None, description="YOLO detection details")
    ocr: Optional[OCRResult] = Field(default=None, description="OCR recognition details")
    processing_time_ms: int = Field(..., description="Total processing time in milliseconds")

    class Config:
        json_schema_extra = {
            "example": {
                "status": "SUCCESS",
                "message": "Placa vehicular reconocida exitosamente",
                "timestamp": "2026-03-07T10:30:00",
                "plate_number": "ABC-123",
                "detection": {
                    "bbox": {"x": 100, "y": 150, "width": 200, "height": 80},
                    "confidence": 0.95
                },
                "ocr": {
                    "text": "ABC-123",
                    "confidence": 0.92
                },
                "processing_time_ms": 245
            }
        }