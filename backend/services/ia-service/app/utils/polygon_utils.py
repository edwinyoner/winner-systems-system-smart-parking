# ia-service/app/utils/polygon_utils.py

"""
Utilidades para manejo de polígonos de estacionamiento.

Este módulo proporciona funciones para:
- Validar y cargar definiciones de polígonos (espacios de parking)
- Recortar regiones de imagen según polígonos
- Verificar si un punto está dentro de un polígono
- Dibujar polígonos en imágenes (debugging)
"""

import json
import logging
from pathlib import Path
from typing import Dict, List, Tuple, Optional, Union

import cv2
import numpy as np
from pydantic import BaseModel, Field, validator

logger = logging.getLogger(__name__)


# ============================================================================
# MODELOS DE DATOS (Pydantic)
# ============================================================================

class PolygonPoint(BaseModel):
    """
    Representa un punto (x, y) de un polígono.
    """
    x: float = Field(..., ge=0, description="Coordenada X del punto")
    y: float = Field(..., ge=0, description="Coordenada Y del punto")

    def to_tuple(self) -> Tuple[float, float]:
        """Convierte a tupla (x, y)"""
        return (self.x, self.y)


class ParkingPolygon(BaseModel):
    """
    Representa un polígono de espacio de estacionamiento.

    Cada polígono define las coordenadas de un espacio (ej: A-001, A-002).
    Debe tener al menos 3 puntos para formar un polígono válido.
    """
    space_id: str = Field(..., description="ID único del espacio (ej: A-001)")
    points: List[List[float]] = Field(
        ...,
        min_items=3,
        description="Lista de puntos [[x1,y1], [x2,y2], ...]"
    )

    @validator('points')
    def validate_points_format(cls, points):
        """Valida que cada punto tenga exactamente 2 coordenadas [x, y]"""
        for i, point in enumerate(points):
            if len(point) != 2:
                raise ValueError(
                    f"Punto {i} inválido: debe tener 2 coordenadas [x, y], "
                    f"pero tiene {len(point)}"
                )
            if not all(isinstance(coord, (int, float)) and coord >= 0 for coord in point):
                raise ValueError(
                    f"Punto {i} inválido: coordenadas deben ser números >= 0"
                )
        return points

    def to_numpy_array(self) -> np.ndarray:
        """
        Convierte los puntos a array numpy para OpenCV.

        Returns:
            np.ndarray: Array de forma (n_points, 1, 2) para cv2.fillPoly
        """
        return np.array(self.points, dtype=np.int32).reshape((-1, 1, 2))

    def get_bounding_box(self) -> Tuple[int, int, int, int]:
        """
        Calcula el bounding box (caja delimitadora) del polígono.

        Returns:
            Tuple[x_min, y_min, x_max, y_max]
        """
        points_array = np.array(self.points)
        x_coords = points_array[:, 0]
        y_coords = points_array[:, 1]

        return (
            int(x_coords.min()),
            int(y_coords.min()),
            int(x_coords.max()),
            int(y_coords.max())
        )


class ParkingPolygonSet(BaseModel):
    """
    Conjunto completo de polígonos para un parking.

    Formato esperado del JSON:
    {
      "A-001": [[x1,y1], [x2,y2], [x3,y3], [x4,y4]],
      "A-002": [[x1,y1], [x2,y2], [x3,y3], [x4,y4]],
      ...
    }
    """
    polygons: Dict[str, List[List[float]]] = Field(
        ...,
        description="Diccionario de polígonos {space_id: [[x,y], ...]}"
    )

    @validator('polygons')
    def validate_polygons_format(cls, polygons):
        """Valida que cada polígono tenga al menos 3 puntos"""
        for space_id, points in polygons.items():
            if len(points) < 3:
                raise ValueError(
                    f"Polígono '{space_id}' inválido: debe tener al menos 3 puntos, "
                    f"tiene {len(points)}"
                )
        return polygons

    def get_polygon(self, space_id: str) -> Optional[ParkingPolygon]:
        """
        Obtiene un polígono específico por ID.

        Args:
            space_id: ID del espacio (ej: "A-001")

        Returns:
            ParkingPolygon o None si no existe
        """
        if space_id not in self.polygons:
            return None

        return ParkingPolygon(
            space_id=space_id,
            points=self.polygons[space_id]
        )

    def get_all_polygons(self) -> List[ParkingPolygon]:
        """
        Obtiene todos los polígonos como lista.

        Returns:
            Lista de ParkingPolygon
        """
        return [
            ParkingPolygon(space_id=space_id, points=points)
            for space_id, points in self.polygons.items()
        ]

    def get_total_spaces(self) -> int:
        """Retorna el número total de espacios"""
        return len(self.polygons)


# ============================================================================
# FUNCIONES DE CARGA Y VALIDACIÓN
# ============================================================================

def load_polygons_from_file(file_path: Union[str, Path]) -> ParkingPolygonSet:
    """
    Carga polígonos desde un archivo JSON.

    Formato esperado del JSON:
    {
      "A-001": [[100,200], [150,200], [150,250], [100,250]],
      "A-002": [[160,200], [210,200], [210,250], [160,250]],
      ...
    }

    Args:
        file_path: Ruta al archivo JSON

    Returns:
        ParkingPolygonSet validado

    Raises:
        FileNotFoundError: Si el archivo no existe
        ValueError: Si el JSON es inválido
        ValidationError: Si los polígonos no cumplen validaciones
    """
    file_path = Path(file_path)

    if not file_path.exists():
        raise FileNotFoundError(f"Archivo de polígonos no encontrado: {file_path}")

    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            data = json.load(f)

        logger.info(f"Cargados {len(data)} polígonos desde {file_path}")
        return ParkingPolygonSet(polygons=data)

    except json.JSONDecodeError as e:
        raise ValueError(f"Error al parsear JSON: {e}")


def load_polygons_from_dict(polygons_dict: Dict[str, List[List[float]]]) -> ParkingPolygonSet:
    """
    Carga polígonos desde un diccionario (request body).

    Args:
        polygons_dict: Diccionario con formato {space_id: [[x,y], ...]}

    Returns:
        ParkingPolygonSet validado

    Raises:
        ValidationError: Si los polígonos no cumplen validaciones
    """
    if not polygons_dict:
        raise ValueError("El diccionario de polígonos está vacío")

    logger.info(f"Validando {len(polygons_dict)} polígonos desde diccionario")
    return ParkingPolygonSet(polygons=polygons_dict)


def save_polygons_to_file(
        polygon_set: ParkingPolygonSet,
        file_path: Union[str, Path]
) -> None:
    """
    Guarda polígonos a un archivo JSON.

    Args:
        polygon_set: Conjunto de polígonos a guardar
        file_path: Ruta donde guardar el archivo
    """
    file_path = Path(file_path)
    file_path.parent.mkdir(parents=True, exist_ok=True)

    with open(file_path, 'w', encoding='utf-8') as f:
        json.dump(polygon_set.polygons, f, indent=2, ensure_ascii=False)

    logger.info(f"Guardados {polygon_set.get_total_spaces()} polígonos en {file_path}")


# ============================================================================
# FUNCIONES GEOMÉTRICAS
# ============================================================================

def point_in_polygon(point: Tuple[float, float], polygon: ParkingPolygon) -> bool:
    """
    Verifica si un punto está dentro de un polígono usando ray casting algorithm.

    Args:
        point: Tupla (x, y) del punto a verificar
        polygon: ParkingPolygon

    Returns:
        True si el punto está dentro del polígono
    """
    x, y = point
    points_array = np.array(polygon.points, dtype=np.float32)

    # Usar cv2.pointPolygonTest (más eficiente que implementar ray casting)
    result = cv2.pointPolygonTest(points_array, (x, y), measureDist=False)

    # result >= 0: punto dentro o en borde del polígono
    # result < 0: punto fuera del polígono
    return result >= 0


def crop_polygon_region(
        image: np.ndarray,
        polygon: ParkingPolygon,
        margin: int = 0
) -> Tuple[np.ndarray, Tuple[int, int, int, int]]:
    """
    Recorta la región de una imagen correspondiente a un polígono.

    Aplica una máscara para que solo se vea el área dentro del polígono.
    El resto queda en negro (0).

    Args:
        image: Imagen original (numpy array BGR)
        polygon: ParkingPolygon a recortar
        margin: Píxeles extra alrededor del bounding box (default: 0)

    Returns:
        Tupla (imagen_recortada, (x_min, y_min, x_max, y_max))
    """
    # Obtener bounding box del polígono
    x_min, y_min, x_max, y_max = polygon.get_bounding_box()

    # Aplicar margen (sin salirse de la imagen)
    h, w = image.shape[:2]
    x_min = max(0, x_min - margin)
    y_min = max(0, y_min - margin)
    x_max = min(w, x_max + margin)
    y_max = min(h, y_max + margin)

    # Crear máscara del polígono
    mask = np.zeros(image.shape[:2], dtype=np.uint8)
    points_array = polygon.to_numpy_array()
    cv2.fillPoly(mask, [points_array], 255)

    # Aplicar máscara a la imagen original
    masked_image = cv2.bitwise_and(image, image, mask=mask)

    # Recortar región del bounding box
    cropped = masked_image[y_min:y_max, x_min:x_max]

    return cropped, (x_min, y_min, x_max, y_max)


def get_polygon_center(polygon: ParkingPolygon) -> Tuple[int, int]:
    """
    Calcula el centro (centroide) de un polígono.

    Args:
        polygon: ParkingPolygon

    Returns:
        Tupla (x_center, y_center)
    """
    points_array = np.array(polygon.points)
    center_x = int(points_array[:, 0].mean())
    center_y = int(points_array[:, 1].mean())
    return (center_x, center_y)


# ============================================================================
# FUNCIONES DE VISUALIZACIÓN (DEBUGGING)
# ============================================================================

def draw_polygons_on_image(
        image: np.ndarray,
        polygon_set: ParkingPolygonSet,
        occupied_spaces: Optional[Dict[str, bool]] = None,
        color_available: Tuple[int, int, int] = (0, 255, 0),  # Verde
        color_occupied: Tuple[int, int, int] = (0, 0, 255),  # Rojo
        thickness: int = 2,
        show_labels: bool = True,
        alpha: float = 0.3
) -> np.ndarray:
    """
    Dibuja polígonos en una imagen para visualización.

    Args:
        image: Imagen BGR original
        polygon_set: Conjunto de polígonos
        occupied_spaces: Dict {space_id: is_occupied}. Si None, todos verdes
        color_available: Color RGB para espacios libres (default: verde)
        color_occupied: Color RGB para espacios ocupados (default: rojo)
        thickness: Grosor de líneas (default: 2)
        show_labels: Si mostrar IDs de espacios (default: True)
        alpha: Transparencia del relleno (0-1, default: 0.3)

    Returns:
        Imagen con polígonos dibujados
    """
    # Crear copia para no modificar original
    overlay = image.copy()
    output = image.copy()

    for polygon in polygon_set.get_all_polygons():
        space_id = polygon.space_id

        # Determinar color según ocupación
        if occupied_spaces and space_id in occupied_spaces:
            color = color_occupied if occupied_spaces[space_id] else color_available
        else:
            color = color_available

        points_array = polygon.to_numpy_array()

        # Dibujar polígono relleno (con transparencia)
        cv2.fillPoly(overlay, [points_array], color)

        # Dibujar borde del polígono
        cv2.polylines(output, [points_array], isClosed=True, color=color, thickness=thickness)

        # Dibujar etiqueta (ID del espacio)
        if show_labels:
            center_x, center_y = get_polygon_center(polygon)

            # Fondo negro para mejor legibilidad
            (text_width, text_height), _ = cv2.getTextSize(
                space_id,
                cv2.FONT_HERSHEY_SIMPLEX,
                0.5,
                1
            )
            cv2.rectangle(
                output,
                (center_x - text_width // 2 - 2, center_y - text_height // 2 - 2),
                (center_x + text_width // 2 + 2, center_y + text_height // 2 + 2),
                (0, 0, 0),
                -1
            )

            # Texto blanco
            cv2.putText(
                output,
                space_id,
                (center_x - text_width // 2, center_y + text_height // 2),
                cv2.FONT_HERSHEY_SIMPLEX,
                0.5,
                (255, 255, 255),
                1,
                cv2.LINE_AA
            )

    # Aplicar transparencia
    cv2.addWeighted(overlay, alpha, output, 1 - alpha, 0, output)

    return output


def draw_occupancy_stats(
        image: np.ndarray,
        total_spaces: int,
        occupied_count: int,
        available_count: int,
        position: Tuple[int, int] = (10, 30),
        font_scale: float = 0.7,
        thickness: int = 2
) -> np.ndarray:
    """
    Dibuja estadísticas de ocupación en la imagen.

    Args:
        image: Imagen BGR
        total_spaces: Total de espacios
        occupied_count: Espacios ocupados
        available_count: Espacios disponibles
        position: Posición (x, y) del texto
        font_scale: Tamaño de fuente
        thickness: Grosor del texto

    Returns:
        Imagen con estadísticas dibujadas
    """
    output = image.copy()
    x, y = position

    # Fondo semi-transparente
    overlay = output.copy()
    cv2.rectangle(overlay, (x - 5, y - 25), (x + 250, y + 50), (0, 0, 0), -1)
    cv2.addWeighted(overlay, 0.7, output, 0.3, 0, output)

    # Texto de estadísticas
    cv2.putText(
        output,
        f"Occupancy: {occupied_count}",
        (x, y),
        cv2.FONT_HERSHEY_SIMPLEX,
        font_scale,
        (0, 0, 255),  # Rojo
        thickness,
        cv2.LINE_AA
    )

    cv2.putText(
        output,
        f"Available: {available_count}",
        (x, y + 30),
        cv2.FONT_HERSHEY_SIMPLEX,
        font_scale,
        (0, 255, 0),  # Verde
        thickness,
        cv2.LINE_AA
    )

    return output


# ============================================================================
# FUNCIONES DE UTILIDAD
# ============================================================================

def validate_image_size(
        image: np.ndarray,
        polygon_set: ParkingPolygonSet
) -> bool:
    """
    Valida que todos los polígonos estén dentro de los límites de la imagen.

    Args:
        image: Imagen numpy array
        polygon_set: Conjunto de polígonos

    Returns:
        True si todos los polígonos son válidos

    Raises:
        ValueError: Si algún polígono está fuera de la imagen
    """
    h, w = image.shape[:2]

    for polygon in polygon_set.get_all_polygons():
        x_min, y_min, x_max, y_max = polygon.get_bounding_box()

        if x_min < 0 or y_min < 0 or x_max > w or y_max > h:
            raise ValueError(
                f"Polígono '{polygon.space_id}' fuera de límites de imagen. "
                f"Imagen: {w}x{h}, Polígono bbox: ({x_min},{y_min})-({x_max},{y_max})"
            )

    logger.info(f"Validados {polygon_set.get_total_spaces()} polígonos dentro de imagen {w}x{h}")
    return True


def convert_ultralytics_format(
        ultralytics_json_path: Union[str, Path]
) -> ParkingPolygonSet:
    """
    Convierte el formato JSON generado por ParkingPtsSelection() de Ultralytics
    al formato usado en este módulo.

    El formato de Ultralytics es el mismo que usamos, pero esta función
    permite validaciones adicionales.

    Args:
        ultralytics_json_path: Ruta al bounding_boxes.json de Ultralytics

    Returns:
        ParkingPolygonSet validado
    """
    return load_polygons_from_file(ultralytics_json_path)