"""
Configuración central y utilidades core
"""
from app.core.config import settings
from app.core.logging import logger
from app.core.constants import *
from app.core.exceptions import (
    IAServiceException,
    ImageProcessingException,
    ModelLoadException,
    YOLODetectionException,
    OCRException,
    ValidationException,
    DatabaseException,
    BadRequestException,
    NotFoundException,
    InternalServerException,
    ServiceUnavailableException
)

__all__ = [
    "settings",
    "logger",
    # Exceptions
    "IAServiceException",
    "ImageProcessingException",
    "ModelLoadException",
    "YOLODetectionException",
    "OCRException",
    "ValidationException",
    "DatabaseException",
    "BadRequestException",
    "NotFoundException",
    "InternalServerException",
    "ServiceUnavailableException",
]