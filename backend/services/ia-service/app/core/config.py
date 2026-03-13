# ia-service/app/core/config.py

"""
Configuration settings for IA Service
Loads environment variables from .env file
"""
from pydantic_settings import BaseSettings
from pydantic import Field  # ← ESTO FALTABA
from typing import List


class Settings(BaseSettings):
    """Application settings loaded from environment variables"""

    # ====================================
    # FastAPI Configuration
    # ====================================
    APP_NAME: str = "IA Service - Smart Parking"
    APP_VERSION: str = "1.0.0"
    APP_HOST: str = "0.0.0.0"
    APP_PORT: int = 8084
    DEBUG: bool = True

    # ====================================
    # CORS Configuration
    # ====================================
    CORS_ORIGINS: List[str] = ["http://localhost:4200", "http://localhost:8080"]

    # ====================================
    # MongoDB Configuration
    # ====================================
    MONGODB_URL: str = "mongodb://localhost:27017"
    MONGODB_DB_NAME: str = "smart_parking_ia"
    MONGODB_MIN_POOL_SIZE: int = 10
    MONGODB_MAX_POOL_SIZE: int = 100

    # ====================================
    # Deep Learning - YOLO Configuration
    # ====================================
    YOLO_MODEL_PATH: str = "yolo26n.pt"
    YOLO_CONFIDENCE_THRESHOLD: float = 0.25
    YOLO_IOU_THRESHOLD: float = 0.45
    YOLO_MAX_DETECTIONS: int = 10
    YOLO_DEVICE: str = "cpu"

    # ====================================
    # Deep Learning - OCR Configuration
    # ====================================
    OCR_LANGUAGES: List[str] = ["en", "es"]
    OCR_GPU: bool = False
    OCR_MIN_CONFIDENCE: float = 0.5
    OCR_PARAGRAPH: bool = False

    # ====================================
    # Image Processing
    # ====================================
    MAX_IMAGE_SIZE_MB: int = 10
    ALLOWED_IMAGE_FORMATS: List[str] = ["jpg", "jpeg", "png"]
    IMAGE_RESIZE_WIDTH: int = 640
    IMAGE_RESIZE_HEIGHT: int = 640

    # ====================================
    # Logging Configuration
    # ====================================
    LOG_LEVEL: str = "INFO"
    LOG_FORMAT: str = "%(asctime)s - %(name)s - %(levelname)s - %(message)s"
    LOG_FILE_PATH: str = "logs/ia-service.log"
    LOG_MAX_BYTES: int = 10485760
    LOG_BACKUP_COUNT: int = 5

    # ====================================
    # Eureka Configuration
    # ====================================
    EUREKA_SERVER_URL: str = "http://localhost:8761/eureka"
    SERVICE_NAME: str = "ia-service"
    SERVICE_INSTANCE_ID: str = "ia-service-1"

    # ====================================
    # S3/MinIO Configuration
    # ====================================
    S3_ENDPOINT: str = "localhost:9000"
    S3_ACCESS_KEY: str = "minioadmin"
    S3_SECRET_KEY: str = "minioadmin"
    S3_BUCKET_NAME: str = "smart-parking"
    S3_SECURE: bool = False  # True para HTTPS

    # ====================================
    # Device Configuration (CPU/GPU)
    # ====================================
    DEVICE: str = "cpu"  # 'cpu' o 'cuda:0' para GPU

    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"
        case_sensitive = True


# Global settings instance
settings = Settings()