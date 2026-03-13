"""
Utilidades para procesamiento de imágenes
"""
import cv2
import numpy as np
from PIL import Image
from io import BytesIO
from typing import Tuple, Optional
from app.core.config import settings
from app.core.exceptions import ImageProcessingException
from app.core.logging import logger


def read_image_from_bytes(image_bytes: bytes) -> np.ndarray:
    """
    Lee una imagen desde bytes y la convierte a formato OpenCV

    Args:
        image_bytes: Bytes de la imagen

    Returns:
        Imagen en formato numpy array (BGR)

    Raises:
        ImageProcessingException: Si no se puede leer la imagen
    """
    try:
        # Convertir bytes a PIL Image
        image = Image.open(BytesIO(image_bytes))

        # Convertir PIL a numpy array
        image_np = np.array(image)

        # Convertir RGB a BGR (formato OpenCV)
        if len(image_np.shape) == 3 and image_np.shape[2] == 3:
            image_bgr = cv2.cvtColor(image_np, cv2.COLOR_RGB2BGR)
        else:
            image_bgr = image_np

        return image_bgr

    except Exception as e:
        logger.error(f"Error al leer imagen desde bytes: {str(e)}")
        raise ImageProcessingException(
            message="No se pudo leer la imagen",
            details={"error": str(e)}
        )


def resize_image(image: np.ndarray, target_size: int = 640) -> np.ndarray:
    """
    Redimensiona una imagen manteniendo el aspect ratio

    Args:
        image: Imagen en formato numpy array
        target_size: Tamaño objetivo (por defecto 640 para YOLO)

    Returns:
        Imagen redimensionada
    """
    height, width = image.shape[:2]

    # Calcular nuevo tamaño manteniendo aspect ratio
    if width > height:
        new_width = target_size
        new_height = int((target_size / width) * height)
    else:
        new_height = target_size
        new_width = int((target_size / height) * width)

    # Redimensionar
    resized = cv2.resize(image, (new_width, new_height), interpolation=cv2.INTER_LINEAR)

    return resized


def validate_image_format(filename: str) -> bool:
    """
    Valida que el formato de imagen sea permitido

    Args:
        filename: Nombre del archivo

    Returns:
        True si el formato es válido
    """
    extension = filename.lower().split('.')[-1]
    return extension in settings.ALLOWED_IMAGE_FORMATS


def validate_image_size(image_bytes: bytes) -> bool:
    """
    Valida que el tamaño de la imagen no exceda el máximo permitido

    Args:
        image_bytes: Bytes de la imagen

    Returns:
        True si el tamaño es válido
    """
    size_mb = len(image_bytes) / (1024 * 1024)
    return size_mb <= settings.MAX_IMAGE_SIZE_MB


def crop_image(image: np.ndarray, bbox: dict) -> np.ndarray:
    """
    Recorta una región de la imagen usando bounding box

    Args:
        image: Imagen completa
        bbox: Diccionario con {x, y, width, height}

    Returns:
        Imagen recortada
    """
    x = bbox['x']
    y = bbox['y']
    w = bbox['width']
    h = bbox['height']

    # Asegurar que las coordenadas estén dentro de la imagen
    height, width = image.shape[:2]
    x = max(0, min(x, width))
    y = max(0, min(y, height))
    w = min(w, width - x)
    h = min(h, height - y)

    # Recortar
    cropped = image[y:y + h, x:x + w]

    return cropped


def image_to_bytes(image: np.ndarray, format: str = 'JPEG') -> bytes:
    """
    Convierte una imagen numpy a bytes

    Args:
        image: Imagen en formato numpy array
        format: Formato de salida (JPEG, PNG)

    Returns:
        Bytes de la imagen
    """
    # Convertir BGR a RGB
    if len(image.shape) == 3 and image.shape[2] == 3:
        image_rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
    else:
        image_rgb = image

    # Convertir a PIL
    pil_image = Image.fromarray(image_rgb)

    # Convertir a bytes
    buffer = BytesIO()
    pil_image.save(buffer, format=format)
    return buffer.getvalue()