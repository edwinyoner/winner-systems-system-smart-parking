# 🤖 IA Service - System Smart Parking

Servicio de Inteligencia Artificial para el reconocimiento automático de placas vehiculares utilizando Deep Learning (YOLO26 + EasyOCR).

---

## 📋 Descripción

Este microservicio implementa algoritmos de **Deep Learning** para:

- **Detección de placas vehiculares** mediante YOLO26 (CNN)
- **Reconocimiento de caracteres** mediante EasyOCR (CNN + RNN)
- **Análisis y métricas** de reconocimiento en tiempo real

---

## 🏗️ Arquitectura

### Deep Learning Models
```
app/ml/dl/
├── yolo/          # CNN para detección de objetos
│   └── detector.py
└── ocr/           # CNN + RNN para OCR
    └── reader.py
```

### Tecnologías

- **Framework:** FastAPI 0.115.5
- **Deep Learning:** PyTorch 2.10.0
- **Object Detection:** Ultralytics YOLO26
- **OCR:** EasyOCR 1.7.2
- **Database:** MongoDB (Motor async)
- **Python:** 3.10+

---

## 🚀 Instalación

### 1. Crear entorno virtual
```bash
python3 -m venv venv
source venv/bin/activate  # Mac/Linux
# venv\Scripts\activate   # Windows
```

### 2. Instalar dependencias
```bash
pip install -r requirements.txt
```

### 3. Configurar variables de entorno
```bash
cp .env.example .env
# Editar .env con tus configuraciones
```

### 4. Descargar modelos de Deep Learning
```bash
python scripts/download_models.py
```

---

## ▶️ Ejecución

### Desarrollo
```bash
uvicorn app.main:app --reload --port 8084
```

### Producción
```bash
uvicorn app.main:app --host 0.0.0.0 --port 8084
```

---

## 🧪 Tests
```bash
pytest tests/ -v
```

---

## 📡 API Endpoints

### Reconocimiento de Placas
```http
POST /api/recognition
Content-Type: multipart/form-data

{
  "image": <file>
}
```

### Métricas
```http
GET /api/analytics/metrics
```

---

## 🎓 Tesis

**Proyecto:** Plataforma Inteligente de Estacionamiento  
**Universidad:** [Tu Universidad]  
**Autor:** Edwin Yoner  
**Año:** 2026

---

## 📄 Licencia

Winner Systems Corporation © 2026
