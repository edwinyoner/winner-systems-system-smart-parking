"""
Script para configurar polígonos de estacionamiento usando Ultralytics.

Uso:
    python configure_polygons.py

Requisitos:
    - Ultralytics instalado (pip install ultralytics)
    - Tkinter instalado (viene con Python)
    - Imagen del parking en ../images/
"""

from ultralytics import solutions
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def main():
    """
    Lanza GUI de Ultralytics para seleccionar polígonos.

    Pasos:
    1. Se abre ventana GUI
    2. Click "Upload Image" → seleccionar foto del dron
    3. Click 4 veces en cada espacio (dibuja polígono)
    4. Repetir para todos los espacios
    5. Click "Save" → guarda bounding_boxes.json
    """
    logger.info("=" * 60)
    logger.info("CONFIGURACIÓN DE POLÍGONOS DE ESTACIONAMIENTO")
    logger.info("=" * 60)
    logger.info("")
    logger.info("INSTRUCCIONES:")
    logger.info("1. Click 'Upload Image' → seleccionar foto del dron")
    logger.info("2. Click 4 veces en cada espacio (esquinas del polígono)")
    logger.info("3. Repetir para TODOS los espacios")
    logger.info("4. Click 'Save' → guarda JSON en directorio actual")
    logger.info("")
    logger.info("IMPORTANTE:")
    logger.info("   - Nombrar espacios: A-001, A-002, B-001, etc.")
    logger.info("   - Clic en orden: esquina superior izquierda → horario")
    logger.info("   - Parking vacío da mejores resultados")
    logger.info("")
    logger.info("Abriendo GUI...")
    logger.info("=" * 60)

    # Lanzar GUI de Ultralytics
    solutions.ParkingPtsSelection()

    logger.info("")
    logger.info("Configuración completa")
    logger.info("Archivo generado: bounding_boxes.json")
    logger.info("")
    logger.info("PRÓXIMOS PASOS:")
    logger.info("1. Mover bounding_boxes.json a ../output/")
    logger.info("2. Renombrar: parking-{id}-polygons.json")
    logger.info("3. Validar con: python validate_polygons.py")
    logger.info("4. Cargar a parking-service")


if __name__ == "__main__":
    main()