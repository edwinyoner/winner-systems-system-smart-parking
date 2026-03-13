"""
Deep Learning models - YOLO26 + EasyOCR
"""
from app.ml.dl.yolo import YOLO26Detector
from app.ml.dl.ocr import EasyOCRReader

__all__ = [
    "YOLO26Detector",
    "EasyOCRReader",
]