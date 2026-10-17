-- ==============================================================================
-- Oracle Initialization - parking-service
-- ==============================================================================

-- Conectar a PDB (Pluggable Database)
ALTER SESSION SET CONTAINER = XEPDB1;

-- Crear usuario de aplicación
CREATE USER parking_user IDENTIFIED BY parking_password;

-- Otorgar privilegios
GRANT CONNECT, RESOURCE TO parking_user;
GRANT CREATE SESSION TO parking_user;
GRANT CREATE TABLE TO parking_user;
GRANT CREATE SEQUENCE TO parking_user;
GRANT CREATE VIEW TO parking_user;
GRANT UNLIMITED TABLESPACE TO parking_user;

-- Verificar
SELECT 'Usuario parking_user creado exitosamente' AS status FROM DUAL;

EXIT;
