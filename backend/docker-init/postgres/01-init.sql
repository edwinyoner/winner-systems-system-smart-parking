-- ==============================================================================
-- PostgreSQL Initialization - auth-service
-- ==============================================================================

-- Crear base de datos auth_db (si no existe)
CREATE DATABASE auth_service_db;

-- Conectar a auth_db y crear esquema
\c auth_db;

-- Aquí puedes agregar tablas iniciales si las necesitas
-- Por ahora, Spring Boot creará las tablas automáticamente con Hibernate

-- Confirmar creación
SELECT current_database();
