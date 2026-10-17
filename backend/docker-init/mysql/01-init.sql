-- ==============================================================================
-- MySQL Initialization - iot-service
-- ==============================================================================

-- Crear base de datos si no existe
CREATE DATABASE IF NOT EXISTS iot_service_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- Crear usuario específico para iot-service con acceso desde cualquier host
CREATE USER IF NOT EXISTS 'iot_user'@'%' IDENTIFIED BY 'iot_password';

-- Otorgar todos los privilegios sobre la base de datos iot_service_db
GRANT ALL PRIVILEGES ON iot_service_db.* TO 'iot_user'@'%';

-- Permitir que root se conecte desde cualquier host (solo para desarrollo)
-- ⚠️ En producción, esto NO se debe hacer
CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY 'mysql';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;

-- Aplicar cambios
FLUSH PRIVILEGES;

-- Usar la base de datos
USE iot_service_db;

-- Verificación
SELECT
    'Base de datos iot_service_db creada' AS status,
    @@character_set_database AS charset,
    @@collation_database AS collation;

SELECT
            User,
            Host,
            plugin
FROM mysql.user
WHERE User IN ('root', 'iot_user')
ORDER BY User, Host;