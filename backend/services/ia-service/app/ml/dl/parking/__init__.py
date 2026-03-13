# ia-service/app/ml/dl/parking/__init__.py

"""
Módulo de detección de ocupación de estacionamiento.
"""

from .occupancy_detector import (
    ParkingOccupancyDetector,
    SpaceDetection,
    ParkingDetectionResult,
    create_detector_instance,
    detect_from_file
)

__all__ = [
    "ParkingOccupancyDetector",
    "SpaceDetection",
    "ParkingDetectionResult",
    "create_detector_instance",
    "detect_from_file"
]