"""
Machine Learning module
Contiene modelos de Deep Learning para reconocimiento de placas
"""
from app.ml.dl import YOLO26Detector, EasyOCRReader

__all__ = [
    "YOLO26Detector",
    "EasyOCRReader",
]