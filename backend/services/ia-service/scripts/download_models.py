"""
Script para pre-descargar modelos de Deep Learning
Los modelos se descargan automáticamente si no existen
"""
from ultralytics import YOLO
from pathlib import Path


def download_yolo26n():
    """
    Descarga el modelo YOLO26n
    Ultralytics lo descarga automáticamente si no existe
    """
    print("=" * 60)
    print("Descargando modelo YOLO26n...")
    print("=" * 60)

    try:
        # Crear directorio de modelos
        models_dir = Path("app/ml/dl/models")
        models_dir.mkdir(parents=True, exist_ok=True)

        print("Inicializando YOLO26n...")
        print("   (Se descargará automáticamente si no existe)")

        # Ultralytics descarga automáticamente el modelo
        model = YOLO("yolo26n.pt")

        print("Modelo YOLO26n listo")
        print(f"   Ubicación: ~/.ultralytics/weights/yolo26n.pt")
        print(f"   Tamaño: ~6.5 MB")

    except Exception as e:
        print(f"Error: {e}")
        raise


def main():
    print("\nDESCARGA DE MODELOS DE DEEP LEARNING")
    print("=" * 60)

    # Descargar YOLO26n
    download_yolo26n()

    print("\n" + "=" * 60)
    print("NOTA:")
    print("   - YOLO se descargó en: ~/.ultralytics/weights/")
    print("   - EasyOCR se descargará automáticamente en primer uso")
    print("=" * 60)

    print("\nProceso completado")
    print("\nPuedes iniciar el servicio con:")
    print("  uvicorn app.main:app --host 0.0.0.0 --port 8084 --reload")


if __name__ == "__main__":
    main()