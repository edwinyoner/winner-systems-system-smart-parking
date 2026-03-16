package database

import (
	"fmt"
	"time"

	"github.com/edwinyoner/winner-systems-system-smart-parking/backend/services/iot-service/internal/config"
	"github.com/sirupsen/logrus"
	"gorm.io/driver/mysql"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

var DB *gorm.DB

// InitMySQL inicializa la conexión a MySQL
func InitMySQL(cfg *config.Config) error {
	logrus.Info("🔌 Conectando a MySQL...")

	// Construir DSN (Data Source Name)
	dsn := fmt.Sprintf("%s:%s@tcp(%s:%s)/%s?charset=utf8mb4&parseTime=True&loc=Local",
		cfg.MySQL.User,
		cfg.MySQL.Password,
		cfg.MySQL.Host,
		cfg.MySQL.Port,
		cfg.MySQL.Database,
	)

	// Configurar GORM logger
	gormLogger := logger.Default.LogMode(logger.Info)

	// Abrir conexión
	db, err := gorm.Open(mysql.Open(dsn), &gorm.Config{
		Logger: gormLogger,
		NowFunc: func() time.Time {
			return time.Now().UTC()
		},
	})
	if err != nil {
		logrus.Errorf("❌ Error conectando a MySQL: %v", err)
		return fmt.Errorf("error conectando a MySQL: %w", err)
	}

	// Obtener conexión SQL subyacente para configurar pool
	sqlDB, err := db.DB()
	if err != nil {
		logrus.Errorf("❌ Error obteniendo conexión SQL: %v", err)
		return fmt.Errorf("error obteniendo conexión SQL: %w", err)
	}

	// Configurar pool de conexiones
	sqlDB.SetMaxOpenConns(cfg.MySQL.MaxOpenConns)
	sqlDB.SetMaxIdleConns(cfg.MySQL.MaxIdleConns)
	sqlDB.SetConnMaxLifetime(time.Hour)

	// Verificar conexión
	if err := sqlDB.Ping(); err != nil {
		logrus.Errorf("❌ Error haciendo ping a MySQL: %v", err)
		return fmt.Errorf("error haciendo ping a MySQL: %w", err)
	}

	DB = db

	logrus.Info("✅ Conexión a MySQL establecida exitosamente")
	logrus.Infof("📊 Database: %s", cfg.MySQL.Database)
	logrus.Infof("🔧 Max Open Connections: %d", cfg.MySQL.MaxOpenConns)
	logrus.Infof("🔧 Max Idle Connections: %d", cfg.MySQL.MaxIdleConns)

	return nil
}

// CloseMySQL cierra la conexión a MySQL
func CloseMySQL() error {
	if DB == nil {
		return nil
	}

	sqlDB, err := DB.DB()
	if err != nil {
		return err
	}

	logrus.Info("🔌 Cerrando conexión a MySQL...")
	return sqlDB.Close()
}

// GetDB retorna la instancia de la base de datos
func GetDB() *gorm.DB {
	return DB
}

// HealthCheck verifica el estado de la conexión a MySQL
func HealthCheck() error {
	if DB == nil {
		return fmt.Errorf("conexión a MySQL no inicializada")
	}

	sqlDB, err := DB.DB()
	if err != nil {
		return err
	}

	return sqlDB.Ping()
}
