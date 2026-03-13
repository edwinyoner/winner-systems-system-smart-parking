"""
Constants for IA Service
"""

# ====================================
# License Plate Patterns (Peru)
# ====================================
PERU_PLATE_PATTERN = r'^[A-Z0-9]{6,7}$'
PERU_OLD_PLATE_PATTERN = r'^[A-Z]{2}-\d{4}$'
PERU_NEW_PLATE_PATTERN = r'^[A-Z]{3}-\d{3}$'

# ====================================
# Image Processing
# ====================================
SUPPORTED_IMAGE_FORMATS = ['jpg', 'jpeg', 'png']
MAX_IMAGE_DIMENSION = 4096
MIN_IMAGE_DIMENSION = 100

# ====================================
# YOLO Detection
# ====================================
YOLO_INPUT_SIZE = 640
YOLO_CLASS_LICENSE_PLATE = 0
YOLO_MIN_BBOX_AREA = 100

# ====================================
# OCR Processing
# ====================================
OCR_ALLOWED_CHARS = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
OCR_MIN_TEXT_LENGTH = 6
OCR_MAX_TEXT_LENGTH = 7

# ====================================
# Recognition Status
# ====================================
STATUS_SUCCESS = "SUCCESS"
STATUS_NO_PLATE_DETECTED = "NO_PLATE_DETECTED"
STATUS_OCR_FAILED = "OCR_FAILED"
STATUS_LOW_CONFIDENCE = "LOW_CONFIDENCE"
STATUS_INVALID_FORMAT = "INVALID_FORMAT"
STATUS_ERROR = "ERROR"

# ====================================
# MongoDB Collections
# ====================================
COLLECTION_RECOGNITIONS = "recognitions"
COLLECTION_METRICS = "metrics"
COLLECTION_AUDIT = "audit_logs"

# ====================================
# API Response Messages
# ====================================
MSG_PLATE_RECOGNIZED = "Placa vehicular reconocida exitosamente"
MSG_NO_PLATE_FOUND = "No se detectó ninguna placa en la imagen"
MSG_OCR_FAILED = "No se pudo leer el texto de la placa"
MSG_LOW_CONFIDENCE = "Reconocimiento con baja confianza"
MSG_INVALID_IMAGE = "Formato de imagen no válido"
MSG_IMAGE_TOO_LARGE = "La imagen supera el tamaño máximo permitido"