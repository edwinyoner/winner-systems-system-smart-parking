# 📦 **ARCHIVO: `ai/README.md`**


## AI/ML/DL Tooling & Configuration - System Smart Parking

## 📋 Descripción

Esta carpeta contiene herramientas, scripts y configuraciones relacionadas con Inteligencia Artificial y Machine Learning que **NO son código de producción deployable**.

El código de IA en producción (YOLO26 + EasyOCR) se encuentra en:
```
backend/services/ia-service/
```

Esta carpeta (`ai/`) es para:
- ✅ Configuración de modelos (una vez)
- ✅ Scripts de entrenamiento/fine-tuning
- ✅ Notebooks de experimentación
- ✅ Datasets y anotaciones
- ✅ Modelos custom pre-entrenados

---

## 📁 Estructura
```
ai/
├── README.md                    ← Este archivo
│
├── parking-configuration/       ← Configuración de polígonos de estacionamiento
│   ├── images/                 ← Fotos aéreas del dron (4K)
│   ├── scripts/                ← Scripts de configuración (Ultralytics GUI)
│   │   ├── configure_polygons.py
│   │   ├── validate_polygons.py
│   │   └── requirements.txt
│   └── output/                 ← JSONs generados (polígonos por parking)
│
├── models/                      ← (Futuro) Modelos ML custom
│   └── fine-tuned-yolo-peru-plates/
│
├── datasets/                    ← (Futuro) Datasets para entrenamiento
│   └── peru-license-plates/
│
└── notebooks/                   ← (Futuro) Jupyter notebooks experimentación
    └── plate-detection-experiments.ipynb
```

---

## 🎯 Componentes Actuales

### 1. Parking Configuration

**Propósito:** Generar polígonos de espacios de estacionamiento para detección de ocupación.

**Uso:**
```bash
cd parking-configuration/scripts
python configure_polygons.py
```

**Detalles:** Ver [parking-configuration/README.md](parking-configuration/README.md)

---

## 🔄 Relación con ia-service

```
┌─────────────────────────────────────────────────┐
│ ai/ (Tooling - No deployable)                  │
│                                                 │
│ - Configuración una vez                        │
│ - Generación de polígonos                      │
│ - Fine-tuning de modelos                       │
│ - Experimentación                              │
│                                                 │
│          ↓ OUTPUT (JSON, modelos .pt)          │
│                                                 │
│ backend/services/ia-service/ (Producción)      │
│                                                 │
│ - FastAPI REST API                             │
│ - YOLO26 + EasyOCR en tiempo real              │
│ - Procesamiento de imágenes                    │
│ - Deployable en contenedor Docker             │
└─────────────────────────────────────────────────┘
```

---

## 🚀 Casos de Uso

### Caso 1: Configurar Nuevo Parking

```bash
# 1. Capturar foto con dron
# Guardar en: parking-configuration/images/parking-X.jpg

# 2. Generar polígonos
cd parking-configuration/scripts
python configure_polygons.py

# 3. Validar JSON generado
python validate_polygons.py --json ../output/parking-X-polygons.json

# 4. Cargar a parking-service
curl -X PUT http://localhost:8082/api/parkings/{id}/polygons \
  -H "Content-Type: application/json" \
  -d @../output/parking-X-polygons.json
```

### Caso 2: Fine-tuning YOLO para Placas Peruanas (Futuro)

```bash
# 1. Preparar dataset
cd datasets/peru-license-plates

# 2. Entrenar modelo
python train_yolo_custom.py \
  --data peru_plates.yaml \
  --epochs 100 \
  --batch 16

# 3. Exportar modelo
# Guardar en: models/fine-tuned-yolo-peru-plates/

# 4. Actualizar ia-service
# Copiar modelo a: backend/services/ia-service/app/ml/dl/models/
```

---

## 🛠️ Herramientas Utilizadas

### Actuales
- **Ultralytics YOLO26** - Object detection framework
- **ParkingPtsSelection** - GUI para configurar polígonos
- **Python 3.12** - Lenguaje principal

### Futuras (Planeadas)
- **Jupyter Notebooks** - Experimentación y análisis
- **Roboflow** - Anotación de datasets
- **Weights & Biases** - Tracking de experimentos
- **CVAT** - Anotación de video/imágenes

---

## 📊 Datasets (Futuro)

Cuando se requiera fine-tuning para mejorar precisión con placas peruanas:

```
datasets/
├── peru-license-plates/
│   ├── images/
│   │   ├── train/
│   │   ├── val/
│   │   └── test/
│   ├── labels/
│   │   ├── train/
│   │   ├── val/
│   │   └── test/
│   ├── data.yaml
│   └── README.md
│
└── parking-ocupancy/
    ├── images/
    ├── labels/
    └── data.yaml
```

---

## 🎓 Documentación Tesis - UNASAM

✅ **Metodología científica**
- Proceso de configuración documentado
- Herramientas profesionales (Ultralytics)
- Scripts reproducibles

✅ **Escalabilidad**
- Preparado para fine-tuning futuro
- Estructura para datasets
- Experimentación organizada

✅ **Profesionalismo**
- Separación clara: tooling vs producción
- Documentación completa
- Versionado de modelos

---

## 📝 Notas Importantes

1. **NO es código deployable**
   - Esta carpeta NO va a contenedores Docker
   - Solo `backend/services/ia-service/` se deploya

2. **Configuración manual**
   - Los scripts aquí se ejecutan MANUALMENTE
   - Una vez por parking o cuando se requiera

3. **Outputs versionados**
   - JSONs generados se guardan en Git
   - Modelos custom (.pt) NO van a Git (muy pesados)
   - Usar Git LFS para modelos si es necesario

4. **Seguridad**
   - NO guardar imágenes con placas visibles en Git
   - Sanitizar datasets antes de compartir
   - Respetar GDPR/privacidad

---

## 🔗 Enlaces Relacionados

- [ia-service README](../backend/services/ia-service/README.md)
- [parking-configuration README](parking-configuration/README.md)
- [Ultralytics Docs](https://docs.ultralytics.com)
- [YOLO26 Parking Management](https://docs.ultralytics.com/guides/parking-management/)

---

## 👨‍💻 Autor

**Edwin Yoner Flores Rupay**  
Bachiller en Ingeniería de Sistemas e Informática  
Universidad Nacional Santiago Antúnez de Mayolo (UNASAM)  
Winner Systems Corporation SAC

---

## 📊 **ESTRUCTURA COMPLETA AHORA:**

```
ai/
├── README.md                      
│
└── parking-configuration/
    ├── README.md                 
    ├── images/
    │   └── .gitkeep
    ├── output/
    │   └── .gitkeep
    └── scripts/
        ├── configure_polygons.py   
        ├── validate_polygons.py    
        └── requirements.txt        
```

---