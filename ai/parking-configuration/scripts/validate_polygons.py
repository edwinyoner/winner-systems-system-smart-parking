"""
Valida un archivo JSON de polígonos generado.

Uso:
    python validate_polygons.py --json ../output/parking-1-polygons.json
"""

import json
import argparse
import sys
from pathlib import Path


def validate_polygons(json_path: Path) -> bool:
    """
    Valida formato de polígonos.

    Returns:
        True si es válido, False si hay errores
    """
    if not json_path.exists():
        print(f"Archivo no encontrado: {json_path}")
        return False

    try:
        with open(json_path, 'r', encoding='utf-8') as f:
            data = json.load(f)

        if not isinstance(data, list):
            print("JSON debe ser una lista")
            return False

        print(f"Total espacios: {len(data)}")

        for i, space in enumerate(data):
            if "points" not in space:
                print(f"Espacio {i}: falta campo 'points'")
                return False

            points = space["points"]

            if len(points) < 3:
                print(f"Espacio {i}: debe tener al menos 3 puntos, tiene {len(points)}")
                return False

            for j, point in enumerate(points):
                if len(point) != 2:
                    print(f"Espacio {i}, punto {j}: debe tener 2 coordenadas [x,y]")
                    return False

        print("Formato válido")
        print(f"Polígonos válidos: {len(data)}")

        return True

    except json.JSONDecodeError as e:
        print(f"Error parseando JSON: {e}")
        return False
    except Exception as e:
        print(f"Error: {e}")
        return False


def main():
    parser = argparse.ArgumentParser(description="Validar JSON de polígonos")
    parser.add_argument("--json", required=True, help="Ruta al archivo JSON")

    args = parser.parse_args()
    json_path = Path(args.json)

    print("=" * 60)
    print("VALIDADOR DE POLÍGONOS")
    print("=" * 60)
    print(f"Archivo: {json_path}")
    print()

    if validate_polygons(json_path):
        print()
        print("Validación exitosa")
        print("Siguiente paso: cargar a parking-service")
        sys.exit(0)
    else:
        print()
        print("Validación fallida")
        print("Revisa el archivo y vuelve a intentar")
        sys.exit(1)


if __name__ == "__main__":
    main()