-- ============================================
-- IOT Service - Database Schema
-- MySQL 8.0
-- ============================================

CREATE DATABASE IF NOT EXISTS smart_parking_iot
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smart_parking_iot;

-- ============================================
-- Tabla: devices
-- Dispositivos IoT registrados (ESP32-CAM, ESP32 Display, etc.)
-- ============================================
CREATE TABLE IF NOT EXISTS devices (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(50) NOT NULL UNIQUE COMMENT 'ID único del dispositivo (ej: ESP32-CAM-001)',
    device_type ENUM('ESP32_CAM_FRONTAL', 'ESP32_CAM_AERIAL', 'ESP32_DISPLAY', 'SENSOR_HC_SR04') NOT NULL,
    parking_id BIGINT UNSIGNED COMMENT 'ID del parking asociado (referencia a parking-service)',
    location VARCHAR(200) COMMENT 'Ubicación física del dispositivo',
    ip_address VARCHAR(45) COMMENT 'Dirección IP del dispositivo',
    status ENUM('ACTIVE', 'INACTIVE', 'MAINTENANCE') DEFAULT 'ACTIVE',
    last_heartbeat TIMESTAMP NULL COMMENT 'Último heartbeat recibido',
    metadata JSON COMMENT 'Metadata adicional del dispositivo',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_device_type (device_type),
    INDEX idx_parking_id (parking_id),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='Dispositivos IoT registrados';

-- ============================================
-- Tabla: camera_captures
-- Capturas de ESP32-CAM frontal (entrada/salida)
-- ============================================
CREATE TABLE IF NOT EXISTS camera_captures (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    capture_id VARCHAR(50) NOT NULL UNIQUE COMMENT 'ID único de la captura',
    device_id VARCHAR(50) NOT NULL COMMENT 'ID del dispositivo ESP32-CAM',
    parking_id BIGINT UNSIGNED NOT NULL COMMENT 'ID del parking',
    image_url VARCHAR(500) NOT NULL COMMENT 'URL de la imagen en S3/MinIO',
    capture_type ENUM('ENTRY', 'EXIT') NOT NULL COMMENT 'Tipo de captura',
    
    -- Resultados de IA
    plate_number VARCHAR(20) COMMENT 'Placa detectada por ia-service',
    plate_confidence DECIMAL(5,3) COMMENT 'Confianza OCR (0.000-1.000)',
    
    -- Referencia a parking-service
    transaction_id BIGINT UNSIGNED COMMENT 'ID de la transacción creada en parking-service',
    
    -- Estado del procesamiento
    processing_status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED') DEFAULT 'PENDING',
    error_message TEXT COMMENT 'Mensaje de error si falla',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    
    INDEX idx_device_id (device_id),
    INDEX idx_parking_id (parking_id),
    INDEX idx_capture_type (capture_type),
    INDEX idx_processing_status (processing_status),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (device_id) REFERENCES devices(device_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Capturas de cámaras frontales (entrada/salida)';

-- ============================================
-- Tabla: parking_snapshots
-- Snapshots de ESP32-CAM aérea (vista completa parking)
-- ============================================
CREATE TABLE IF NOT EXISTS parking_snapshots (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    snapshot_id VARCHAR(50) NOT NULL UNIQUE COMMENT 'ID único del snapshot',
    device_id VARCHAR(50) NOT NULL COMMENT 'ID del dispositivo ESP32-CAM aérea',
    parking_id BIGINT UNSIGNED NOT NULL COMMENT 'ID del parking',
    image_url VARCHAR(500) NOT NULL COMMENT 'URL de la imagen en S3/MinIO',
    
    -- Resultados agregados
    total_spaces INT NOT NULL COMMENT 'Total de espacios en el parking',
    occupied_spaces INT NOT NULL COMMENT 'Espacios ocupados detectados',
    available_spaces INT NOT NULL COMMENT 'Espacios disponibles',
    
    -- Estado del procesamiento
    processing_status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED') DEFAULT 'PENDING',
    error_message TEXT COMMENT 'Mensaje de error si falla',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    
    INDEX idx_device_id (device_id),
    INDEX idx_parking_id (parking_id),
    INDEX idx_processing_status (processing_status),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (device_id) REFERENCES devices(device_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Snapshots de vista aérea completa';

-- ============================================
-- Tabla: space_detections
-- Detecciones individuales por espacio (desde snapshot)
-- ============================================
CREATE TABLE IF NOT EXISTS space_detections (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    snapshot_id BIGINT UNSIGNED NOT NULL COMMENT 'ID del snapshot padre',
    space_id VARCHAR(20) NOT NULL COMMENT 'ID del espacio (ej: A-001)',
    occupied BOOLEAN NOT NULL COMMENT 'true=ocupado, false=disponible',
    
    -- Detalles del vehículo (si está ocupado)
    vehicle_type VARCHAR(20) COMMENT 'Tipo de vehículo (car, truck, bus)',
    vehicle_confidence DECIMAL(5,3) COMMENT 'Confianza detección vehículo',
    
    -- Placa (si se detectó desde vista aérea)
    plate_number VARCHAR(20) COMMENT 'Placa leída desde vista aérea',
    plate_confidence DECIMAL(5,3) COMMENT 'Confianza OCR aéreo',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_snapshot_id (snapshot_id),
    INDEX idx_space_id (space_id),
    INDEX idx_occupied (occupied),
    FOREIGN KEY (snapshot_id) REFERENCES parking_snapshots(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Detecciones individuales por espacio';

-- ============================================
-- Tabla: display_logs
-- Log de consultas de displays LED
-- ============================================
CREATE TABLE IF NOT EXISTS display_logs (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(50) NOT NULL COMMENT 'ID del dispositivo ESP32 Display',
    parking_id BIGINT UNSIGNED NOT NULL COMMENT 'ID del parking',
    total_spaces INT NOT NULL,
    occupied_spaces INT NOT NULL,
    available_spaces INT NOT NULL,
    response_time_ms INT COMMENT 'Tiempo de respuesta en ms',
    from_cache BOOLEAN DEFAULT FALSE COMMENT 'true si vino de Redis cache',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_device_id (device_id),
    INDEX idx_parking_id (parking_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB COMMENT='Log de consultas de disponibilidad desde displays LED';

-- ============================================
-- Datos de prueba (opcional)
-- ============================================

-- Dispositivo ESP32-CAM frontal
INSERT INTO devices (device_id, device_type, parking_id, location, ip_address, status) 
VALUES ('ESP32-CAM-FRONTAL-001', 'ESP32_CAM_FRONTAL', 1, 'Entrada principal - Jr. San Martín', '192.168.1.100', 'ACTIVE');

-- Dispositivo ESP32-CAM aérea
INSERT INTO devices (device_id, device_type, parking_id, location, ip_address, status) 
VALUES ('ESP32-CAM-AERIAL-001', 'ESP32_CAM_AERIAL', 1, 'Poste 5m altura - Vista completa', '192.168.1.101', 'ACTIVE');

-- Dispositivo ESP32 Display
INSERT INTO devices (device_id, device_type, parking_id, location, ip_address, status) 
VALUES ('ESP32-DISPLAY-001', 'ESP32_DISPLAY', 1, 'Av. Luzuriaga esq. Jr. Bolívar', '192.168.1.102', 'ACTIVE');

-- ============================================
-- Fin del script
-- ============================================
