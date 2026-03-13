"""
Custom exceptions for IA Service
"""
from fastapi import HTTPException, status


class IAServiceException(Exception):
    """Base exception for IA Service"""
    def __init__(self, message: str, details: dict = None):
        self.message = message
        self.details = details or {}
        super().__init__(self.message)


class ImageProcessingException(IAServiceException):
    """Exception raised when image processing fails"""
    pass


class ModelLoadException(IAServiceException):
    """Exception raised when Deep Learning model fails to load"""
    pass


class YOLODetectionException(IAServiceException):
    """Exception raised when YOLO detection fails"""
    pass


class OCRException(IAServiceException):
    """Exception raised when OCR processing fails"""
    pass


class ValidationException(IAServiceException):
    """Exception raised when validation fails"""
    pass


class DatabaseException(IAServiceException):
    """Exception raised when database operation fails"""
    pass


# HTTP Exceptions
class BadRequestException(HTTPException):
    """400 Bad Request"""
    def __init__(self, detail: str):
        super().__init__(status_code=status.HTTP_400_BAD_REQUEST, detail=detail)


class NotFoundException(HTTPException):
    """404 Not Found"""
    def __init__(self, detail: str):
        super().__init__(status_code=status.HTTP_404_NOT_FOUND, detail=detail)


class InternalServerException(HTTPException):
    """500 Internal Server Error"""
    def __init__(self, detail: str):
        super().__init__(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail=detail)


class ServiceUnavailableException(HTTPException):
    """503 Service Unavailable"""
    def __init__(self, detail: str):
        super().__init__(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail=detail)