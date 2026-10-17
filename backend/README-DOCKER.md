# 🐳 Smart Parking - Docker Database Setup

**Proyecto:** Sistema Smart Parking - UNASAM  
**Autor:** Edwin Yoner Flores Rupay  
**Fecha:** Marzo 2026

---

## 📋 Versiones de Bases de Datos

| Base de Datos | Versión | Imagen Docker |
|---------------|---------|---------------|
| PostgreSQL | 18 (Alpine) | `postgres:18-alpine` |
| MySQL | 9.0 | `container-registry.oracle.com/mysql/community-server:latest` |
| MongoDB | 8.0 | `mongodb/mongodb-community-server:latest` |
| Oracle | 21c XE | `container-registry.oracle.com/database/express:21.3.0-xe` |
| Redis | 7 (Alpine) | `redis:7-alpine` |
| MinIO | Latest | `minio/minio:latest` |

---

## 🚀 Inicio Rápido
```bash
# 1. Ir a la carpeta backend
cd ~/Documents/winner-systems-system-smart-parking/backend

# 2. Crear carpetas para scripts de inicialización (opcional)
mkdir -p docker-init/postgres docker-init/mysql docker-init/mongodb
mkdir -p docker-init/oracle/startup docker-init/oracle/setup

# 3. Levantar todas las bases de datos
docker-compose up -d

# 4. Ver logs en tiempo real
docker-compose logs -f

# 5. Verificar que todo esté corriendo
docker-compose ps
```

---

## 📊 Tabla de Credenciales

| BD | Host | Puerto(s) | Usuario | Password | BD/Service |
|----|------|-----------|---------|----------|------------|
| **PostgreSQL** | localhost | 5432 | `postgres` | `postgres123` | `postgres` |
| **MySQL** | localhost | 3306 | `root` | `mysql123` | `mysql` |
| **MongoDB** | localhost | 27017 | `admin` | `mongo123` | `admin` |
| **Oracle** | localhost | 1521, 5500 | `SYSTEM` | `oracle123` | `XEPDB1` |
| **Redis** | localhost | 6379 | - | `redis123` | - |
| **MinIO** | localhost | 9000, 9001 | `minioadmin` | `minioadmin123` | - |

---

## 🔌 Conexiones desde Servicios

### PostgreSQL (auth-service)
```properties
# application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/auth_db
spring.datasource.username=postgres
spring.datasource.password=postgres123
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```
```bash
# Conectar desde terminal
docker exec -it smart-parking-postgres psql -U postgres

# Crear base de datos
CREATE DATABASE auth_db;
\l
\q
```

---

### MySQL (iot-service)
```env
# .env
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_USER=root
MYSQL_PASSWORD=mysql123
MYSQL_DATABASE=iot_service_db
```
```bash
# Conectar desde terminal
docker exec -it smart-parking-mysql mysql -uroot -pmysql123

# Crear base de datos
CREATE DATABASE iot_service_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
SHOW DATABASES;
EXIT;

# Ejecutar script de inicialización
docker exec -i smart-parking-mysql mysql -uroot -pmysql123 iot_service_db < services/iot-service/scripts/init.sql
```

---

### MongoDB (ia-service)
```python
# app/core/config.py
MONGODB_URL = "mongodb://admin:mongo123@localhost:27017/ia_service_db?authSource=admin"
MONGODB_DB_NAME = "ia_service_db"
```
```bash
# Conectar desde terminal
docker exec -it smart-parking-mongodb mongosh -u admin -p mongo123

# Crear base de datos y usuario
use ia_service_db

db.createUser({
  user: "ia_user",
  pwd: "ia_password",
  roles: [{ role: "readWrite", db: "ia_service_db" }]
})

# Verificar
show dbs
show users
exit
```

---

### Oracle (parking-service)
```properties
# application.properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521/XEPDB1
spring.datasource.username=SYSTEM
spring.datasource.password=oracle123
spring.jpa.database-platform=org.hibernate.dialect.OracleDialect
```
```bash
# Conectar desde terminal
docker exec -it smart-parking-oracle sqlplus SYSTEM/oracle123@XEPDB1

# Crear usuario de aplicación
CREATE USER parking_user IDENTIFIED BY parking123;
GRANT CONNECT, RESOURCE, CREATE VIEW TO parking_user;
GRANT UNLIMITED TABLESPACE TO parking_user;

# Verificar
SELECT username FROM dba_users WHERE username = 'PARKING_USER';
EXIT;
```

**Oracle Enterprise Manager Express:**
- URL: http://localhost:5500/em
- Usuario: `SYSTEM`
- Password: `oracle123`

---

## ⏱️ Tiempos de Inicio

| Servicio | Tiempo | Estado |
|----------|--------|--------|
| PostgreSQL 18 | ~5 seg | ✅ Rápido |
| MySQL 9.0 | ~20 seg | ✅ Rápido |
| MongoDB 8 | ~10 seg | ✅ Rápido |
| Oracle 21c XE | ~2-5 min | ⏳ Primera vez (luego rápido) |
| Redis 7 | ~2 seg | ✅ Instantáneo |
| MinIO | ~3 seg | ✅ Instantáneo |

**⚠️ Oracle tarda más:** Espera hasta ver en los logs:
```
DATABASE IS READY TO USE!
```

---

## 🔧 Comandos Útiles
```bash
# Ver estado de contenedores
docker-compose ps

# Ver logs de un servicio específico
docker-compose logs -f postgres
docker-compose logs -f mysql
docker-compose logs -f mongodb
docker-compose logs -f oracle

# Reiniciar un servicio
docker-compose restart mysql

# Detener todos los servicios
docker-compose down

# Detener y BORRAR volúmenes (⚠️ BORRA DATOS)
docker-compose down -v

# Ver uso de recursos
docker stats

# Acceder al shell de un contenedor
docker exec -it smart-parking-postgres sh
docker exec -it smart-parking-mysql bash
docker exec -it smart-parking-mongodb bash
docker exec -it smart-parking-oracle bash
```

---

## 📦 Actualizar Imágenes
```bash
# Descargar últimas versiones
docker-compose pull

# Recrear contenedores
docker-compose up -d --force-recreate
```

---

## 🔍 Troubleshooting

### Oracle no inicia
```bash
# Verificar logs
docker-compose logs -f oracle

# Oracle requiere mínimo 2GB RAM
# Docker Desktop > Settings > Resources > Memory: 8GB mínimo

# Si falla, eliminar volumen y recrear
docker-compose down -v
docker volume rm backend_oracle_data
docker-compose up -d oracle
```

### MySQL error de conexión
```bash
# Esperar 20 segundos después de docker-compose up
docker-compose ps mysql

# Verificar health check
docker inspect smart-parking-mysql | grep Health -A 10
```

### MongoDB authentication failed
```bash
# Usar authSource=admin
mongosh -u admin -p mongo --authenticationDatabase admin

# Connection string completo
mongodb://admin:mongo@localhost:27017/?authSource=admin
```

### Redis connection refused
```bash
# Verificar que Redis esté corriendo
docker-compose ps redis

# Probar conexión
docker exec -it smart-parking-redis redis-cli -a redis123 PING
# Debe responder: PONG
```

---

## 📖 Recursos y Documentación

- **PostgreSQL 18:** https://www.postgresql.org/docs/18/
- **MySQL 9.0:** https://dev.mysql.com/doc/refman/9.0/en/
- **MongoDB 8:** https://www.mongodb.com/docs/v8.0/
- **Oracle 21c XE:** https://docs.oracle.com/en/database/oracle/oracle-database/21/
- **Redis 7:** https://redis.io/docs/latest/
- **MinIO:** https://min.io/docs/minio/linux/index.html

---

## 🎯 Próximos Pasos

1. ✅ Levantar Docker Compose
2. ✅ Crear bases de datos para cada servicio
3. ✅ Ejecutar scripts de inicialización (init.sql)
4. ✅ Actualizar variables de entorno en servicios
5. ✅ Probar conexiones desde cada microservicio
6. 🚀 ¡Desarrollar!

---

**UNASAM - Facultad de Ingeniería de Sistemas e Informática**  
*"Del esfuerzo de sus hijos depende el progreso de los pueblos"*