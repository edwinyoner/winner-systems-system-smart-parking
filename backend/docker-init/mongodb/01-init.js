// ==============================================================================
// MongoDB Initialization - ia-service
// ==============================================================================

// Cambiar a la base de datos de la aplicación
db = db.getSiblingDB('ia_service_db');

// Crear usuario para la aplicación
db.createUser({
  user: 'ia_user',
  pwd: 'ia_password',
  roles: [
    {
      role: 'readWrite',
      db: 'ia_service_db'
    }
  ]
});

// Crear colecciones iniciales (opcional)
db.createCollection('plate_recognitions');

print('MongoDB ia_service_db inicializada');
