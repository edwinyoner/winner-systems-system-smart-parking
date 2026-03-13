"""
Base schemas for IA Service
"""
from pydantic import BaseModel, Field
from typing import Optional, Any
from datetime import datetime


class BaseResponse(BaseModel):
    """Base response model for all API responses"""
    status: str = Field(..., description="Response status")
    message: str = Field(..., description="Response message")
    timestamp: datetime = Field(default_factory=datetime.now, description="Response timestamp")


class ErrorResponse(BaseModel):
    """Error response model"""
    status: str = Field(default="ERROR", description="Error status")
    message: str = Field(..., description="Error message")
    details: Optional[dict] = Field(default=None, description="Additional error details")
    timestamp: datetime = Field(default_factory=datetime.now, description="Error timestamp")


class BoundingBox(BaseModel):
    """Bounding box coordinates for detected objects"""
    x: int = Field(..., ge=0, description="X coordinate (top-left)")
    y: int = Field(..., ge=0, description="Y coordinate (top-left)")
    width: int = Field(..., gt=0, description="Width of the box")
    height: int = Field(..., gt=0, description="Height of the box")

    class Config:
        json_schema_extra = {
            "example": {
                "x": 100,
                "y": 150,
                "width": 200,
                "height": 80
            }
        }