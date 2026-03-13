# Configuración de Polígonos de Estacionamiento

## Propósito

Esta carpeta contiene herramientas para configurar polígonos de espacios
de estacionamiento usando imágenes aéreas capturadas con dron.

## Proceso

1. **Captura de imagen** (dron DJI/Phantom)
   - Altura: 15-20 metros
   - Resolución: 4K mínimo
   - Vista perpendicular
   - Parking preferiblemente vacío

2. **Generar polígonos** (una vez por parking)
```bash
   cd scripts
   python configure_polygons.py
```

3. **Validar polígonos**
```bash
   python validate_polygons.py --json ../output/parking-1-polygons.json
```

4. **Cargar a parking-service**
   - Via Postman/curl o interfaz Angular (futuro)
   - Endpoint: PUT /parkings/{id}/polygons

## Estructura
```
parking-configuration/
├── images/              ← Fotos originales del dron (4K)
├── scripts/             ← Scripts de configuración
├── output/              ← JSONs generados
└── README.md            ← Este archivo
```

## Tecnologías

- Ultralytics YOLO26 (ParkingPtsSelection)
- Python 3.12
- Tkinter (GUI)