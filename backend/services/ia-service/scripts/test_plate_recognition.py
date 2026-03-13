"""
Script de prueba para reconocimiento de placas vehiculares
Prueba directa del PlateRecognitionService sin HTTP

Uso:
    python scripts/test_plate_recognition.py
    python scripts/test_plate_recognition.py --images-dir data/sample_plates
    python scripts/test_plate_recognition.py --yolo-conf 0.3 --ocr-conf 0.6
"""
import sys
import argparse
from pathlib import Path
from datetime import datetime
from typing import List, Dict
import cv2
import numpy as np

# Agregar directorio raíz al path para imports
sys.path.insert(0, str(Path(__file__).parent.parent))

from app.services.plate_recognition_service import PlateRecognitionService
from app.schemas.recognition import RecognitionRequest, RecognitionResponse
from app.core.logging import logger
from app.utils.image_utils import read_image_from_bytes


class PlateRecognitionTester:
    """
    Tester para validar reconocimiento de placas con imágenes reales
    """

    def __init__(self, yolo_conf: float = 0.25, ocr_conf: float = 0.5):
        """
        Inicializa el tester

        Args:
            yolo_conf: Umbral de confianza YOLO
            ocr_conf: Umbral de confianza OCR
        """
        print("=" * 80)
        print("🤖 TESTER DE RECONOCIMIENTO DE PLACAS VEHICULARES")
        print("=" * 80)
        print(f"📅 Fecha: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        print(f"⚙️  Umbral YOLO: {yolo_conf}")
        print(f"⚙️  Umbral OCR:  {ocr_conf}")
        print("=" * 80)
        print()

        # Inicializar servicio
        print("🔄 Cargando modelos de Deep Learning...")
        self.service = PlateRecognitionService()

        # Configuración de prueba
        self.yolo_conf = yolo_conf
        self.ocr_conf = ocr_conf

        # Métricas
        self.results: List[Dict] = []

        print("Servicio inicializado correctamente")
        print()

    def load_test_images(self, images_dir: str) -> List[Path]:
        """
        Carga imágenes de prueba desde un directorio

        Args:
            images_dir: Ruta al directorio con imágenes

        Returns:
            Lista de rutas de archivos de imagen
        """
        images_path = Path(images_dir)

        if not images_path.exists():
            print(f"ERROR: Directorio no encontrado: {images_dir}")
            print(f"   Crea el directorio y agrega imágenes de placas")
            return []

        # Buscar imágenes (JPG, PNG, JPEG)
        image_files = []
        for ext in ['*.jpg', '*.jpeg', '*.png', '*.JPG', '*.JPEG', '*.PNG']:
            image_files.extend(images_path.glob(ext))

        if not image_files:
            print(f"ADVERTENCIA: No se encontraron imágenes en {images_dir}")
            print(f"   Formatos soportados: JPG, JPEG, PNG")
            return []

        print(f"📁 Imágenes encontradas: {len(image_files)}")
        for i, img_file in enumerate(image_files, 1):
            print(f"   {i}. {img_file.name}")
        print()

        return sorted(image_files)

    def test_single_image(
            self,
            image_path: Path,
            expected_plate: str = None
    ) -> Dict:
        """
        Prueba reconocimiento en una sola imagen

        Args:
            image_path: Ruta a la imagen
            expected_plate: Placa esperada (opcional, para validación)

        Returns:
            Diccionario con resultados
        """
        print("-" * 80)
        print(f"🖼️  Procesando: {image_path.name}")

        if expected_plate:
            print(f"📋 Placa esperada: {expected_plate}")

        print("-" * 80)

        try:
            # Leer imagen con OpenCV
            image_bgr = cv2.imread(str(image_path))

            if image_bgr is None:
                print(f"ERROR: No se pudo leer la imagen")
                return {
                    "filename": image_path.name,
                    "status": "ERROR",
                    "error": "Failed to read image",
                    "expected_plate": expected_plate
                }

            # Crear request
            request = RecognitionRequest(
                yolo_confidence=self.yolo_conf,
                ocr_confidence=self.ocr_conf
            )

            # Ejecutar reconocimiento
            start_time = datetime.now()
            response: RecognitionResponse = self.service.recognize_plate(
                image_bgr,
                request
            )
            end_time = datetime.now()

            processing_time = (end_time - start_time).total_seconds() * 1000

            # Imprimir resultado
            self._print_result(response, expected_plate)

            # Validación
            is_correct = None
            if expected_plate and response.plate_number:
                is_correct = response.plate_number == expected_plate.upper()

            # Guardar resultado
            result = {
                "filename": image_path.name,
                "status": response.status,
                "plate_detected": response.plate_number,
                "expected_plate": expected_plate,
                "is_correct": is_correct,
                "yolo_confidence": response.detection.confidence if response.detection else None,
                "ocr_confidence": response.ocr.confidence if response.ocr else None,
                "processing_time_ms": response.processing_time_ms,
                "actual_processing_time_ms": int(processing_time)
            }

            return result

        except Exception as e:
            print(f"ERROR: {str(e)}")
            logger.error(f"Error procesando {image_path.name}: {str(e)}")

            return {
                "filename": image_path.name,
                "status": "ERROR",
                "error": str(e),
                "expected_plate": expected_plate
            }

    def _print_result(
            self,
            response: RecognitionResponse,
            expected_plate: str = None
    ):
        """Imprime resultado formateado"""

        print(f"\n📊 RESULTADO:")
        print(f"   Status: {response.status}")
        print(f"   Placa detectada: {response.plate_number or 'N/A'}")

        if response.detection:
            print(f"   YOLO confianza: {response.detection.confidence:.3f} "
                  f"({response.detection.confidence * 100:.1f}%)")

        if response.ocr:
            print(f"   OCR confianza:  {response.ocr.confidence:.3f} "
                  f"({response.ocr.confidence * 100:.1f}%)")

        print(f"   Tiempo: {response.processing_time_ms} ms")

        # Validación
        if expected_plate:
            detected = response.plate_number or ""
            expected_upper = expected_plate.upper()

            if detected == expected_upper:
                print(f"   CORRECTO (coincide con esperado)")
            else:
                print(f"   INCORRECTO (esperado: {expected_upper})")

        print()

    def run_batch_test(
            self,
            images_dir: str,
            expected_plates: Dict[str, str] = None
    ):
        """
        Ejecuta prueba en batch con múltiples imágenes

        Args:
            images_dir: Directorio con imágenes
            expected_plates: Diccionario {filename: expected_plate}
        """
        print("🚀 INICIANDO PRUEBA EN BATCH")
        print("=" * 80)
        print()

        # Cargar imágenes
        image_files = self.load_test_images(images_dir)

        if not image_files:
            print("No hay imágenes para procesar")
            return

        # Procesar cada imagen
        for i, img_path in enumerate(image_files, 1):
            print(f"\n[{i}/{len(image_files)}]")

            # Buscar placa esperada
            expected = None
            if expected_plates and img_path.name in expected_plates:
                expected = expected_plates[img_path.name]

            # Procesar
            result = self.test_single_image(img_path, expected)
            self.results.append(result)

        # Generar reporte final
        print("\n\n")
        print("=" * 80)
        print("📈 REPORTE FINAL")
        print("=" * 80)
        self._generate_report()

    def _generate_report(self):
        """Genera reporte con métricas agregadas"""

        if not self.results:
            print("⚠️  No hay resultados para reportar")
            return

        total = len(self.results)
        successful = sum(1 for r in self.results if r['status'] == 'SUCCESS')
        failed = total - successful

        # Métricas de confianza
        yolo_confidences = [r['yolo_confidence'] for r in self.results
                            if r.get('yolo_confidence')]
        ocr_confidences = [r['ocr_confidence'] for r in self.results
                           if r.get('ocr_confidence')]

        # Métricas de tiempo
        processing_times = [r['processing_time_ms'] for r in self.results
                            if r.get('processing_time_ms')]

        # Métricas de precisión (si hay placas esperadas)
        correct = sum(1 for r in self.results if r.get('is_correct') is True)
        incorrect = sum(1 for r in self.results if r.get('is_correct') is False)

        print(f"\n📊 MÉTRICAS GENERALES:")
        print(f"   Total de imágenes: {total}")
        print(f"   Exitosos: {successful} ({successful / total * 100:.1f}%)")
        print(f"   Fallidos:  {failed} ({failed / total * 100:.1f}%)")

        if yolo_confidences:
            avg_yolo = sum(yolo_confidences) / len(yolo_confidences)
            print(f"\n📊 YOLO:")
            print(f"   Confianza promedio: {avg_yolo:.3f} ({avg_yolo * 100:.1f}%)")
            print(f"   Confianza mínima:   {min(yolo_confidences):.3f}")
            print(f"   Confianza máxima:   {max(yolo_confidences):.3f}")

        if ocr_confidences:
            avg_ocr = sum(ocr_confidences) / len(ocr_confidences)
            print(f"\n📊 OCR:")
            print(f"   Confianza promedio: {avg_ocr:.3f} ({avg_ocr * 100:.1f}%)")
            print(f"   Confianza mínima:   {min(ocr_confidences):.3f}")
            print(f"   Confianza máxima:   {max(ocr_confidences):.3f}")

        if processing_times:
            avg_time = sum(processing_times) / len(processing_times)
            print(f"\n⏱️  TIEMPOS DE PROCESAMIENTO:")
            print(f"   Tiempo promedio: {avg_time:.0f} ms")
            print(f"   Tiempo mínimo:   {min(processing_times)} ms")
            print(f"   Tiempo máximo:   {max(processing_times)} ms")

        if correct > 0 or incorrect > 0:
            total_validated = correct + incorrect
            accuracy = correct / total_validated * 100
            print(f"\nPRECISIÓN (con placas esperadas):")
            print(f"   Correctos:   {correct}/{total_validated} ({accuracy:.1f}%)")
            print(f"   Incorrectos: {incorrect}/{total_validated}")

        # Detalles de fallos
        errors = [r for r in self.results if r['status'] != 'SUCCESS']
        if errors:
            print(f"\nDETALLES DE FALLOS:")
            for err in errors:
                print(f"   • {err['filename']}: {err['status']}")

        print("\n" + "=" * 80)

    def export_results_csv(self, output_path: str = "test_results.csv"):
        """Exporta resultados a CSV"""
        import csv

        if not self.results:
            print("⚠️  No hay resultados para exportar")
            return

        with open(output_path, 'w', newline='') as f:
            writer = csv.DictWriter(f, fieldnames=self.results[0].keys())
            writer.writeheader()
            writer.writerows(self.results)

        print(f"📄 Resultados exportados a: {output_path}")


def main():
    """Entry point del script"""
    parser = argparse.ArgumentParser(
        description='Tester de reconocimiento de placas vehiculares'
    )
    parser.add_argument(
        '--images-dir',
        type=str,
        default='data/sample_plates',
        help='Directorio con imágenes de prueba (default: data/sample_plates)'
    )
    parser.add_argument(
        '--yolo-conf',
        type=float,
        default=0.25,
        help='Umbral de confianza YOLO (default: 0.25)'
    )
    parser.add_argument(
        '--ocr-conf',
        type=float,
        default=0.5,
        help='Umbral de confianza OCR (default: 0.5)'
    )
    parser.add_argument(
        '--export-csv',
        type=str,
        default=None,
        help='Exportar resultados a CSV (ej: results.csv)'
    )

    args = parser.parse_args()

    # Crear tester
    tester = PlateRecognitionTester(
        yolo_conf=args.yolo_conf,
        ocr_conf=args.ocr_conf
    )

    # Placas esperadas (opcional - editar según tus imágenes)
    expected_plates = {
        # "IMG_3866.JPG": "ABC-123",
        # "IMG_3875.jpg": "XYZ-789",
        # "placa3.png": "DEF-456",
    }

    # Ejecutar prueba
    tester.run_batch_test(args.images_dir, expected_plates)

    # Exportar resultados
    if args.export_csv:
        tester.export_results_csv(args.export_csv)

    print("\nPrueba completada")


if __name__ == "__main__":
    main()