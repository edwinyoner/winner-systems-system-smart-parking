package database

import (
	"context"
	"fmt"
	"time"

	"github.com/edwinyoner/winner-systems-system-smart-parking/backend/services/iot-service/internal/config"
	"github.com/redis/go-redis/v9"
	"github.com/sirupsen/logrus"
)

var RedisClient *redis.Client
var ctx = context.Background()

// InitRedis inicializa la conexión a Redis
func InitRedis(cfg *config.Config) error {
	logrus.Info("🔌 Conectando a Redis...")

	// Crear cliente Redis
	client := redis.NewClient(&redis.Options{
		Addr:         fmt.Sprintf("%s:%s", cfg.Redis.Host, cfg.Redis.Port),
		Password:     cfg.Redis.Password,
		DB:           cfg.Redis.DB,
		PoolSize:     cfg.Redis.PoolSize,
		MinIdleConns: 5,
		DialTimeout:  5 * time.Second,
		ReadTimeout:  3 * time.Second,
		WriteTimeout: 3 * time.Second,
		PoolTimeout:  4 * time.Second,
	})

	// Verificar conexión
	if err := client.Ping(ctx).Err(); err != nil {
		logrus.Errorf("❌ Error conectando a Redis: %v", err)
		return fmt.Errorf("error conectando a Redis: %w", err)
	}

	RedisClient = client

	logrus.Info("✅ Conexión a Redis establecida exitosamente")
	logrus.Infof("📊 Redis DB: %d", cfg.Redis.DB)
	logrus.Infof("🔧 Pool Size: %d", cfg.Redis.PoolSize)

	return nil
}

// CloseRedis cierra la conexión a Redis
func CloseRedis() error {
	if RedisClient == nil {
		return nil
	}

	logrus.Info("🔌 Cerrando conexión a Redis...")
	return RedisClient.Close()
}

// GetRedis retorna la instancia del cliente Redis
func GetRedis() *redis.Client {
	return RedisClient
}

// HealthCheckRedis verifica el estado de la conexión a Redis
func HealthCheckRedis() error {
	if RedisClient == nil {
		return fmt.Errorf("conexión a Redis no inicializada")
	}

	return RedisClient.Ping(ctx).Err()
}

// CacheSet guarda un valor en cache con TTL
func CacheSet(key string, value interface{}, expiration time.Duration) error {
	return RedisClient.Set(ctx, key, value, expiration).Err()
}

// CacheGet obtiene un valor del cache
func CacheGet(key string) (string, error) {
	return RedisClient.Get(ctx, key).Result()
}

// CacheDel elimina una clave del cache
func CacheDel(keys ...string) error {
	return RedisClient.Del(ctx, keys...).Err()
}

// CacheExists verifica si una clave existe
func CacheExists(key string) (bool, error) {
	result, err := RedisClient.Exists(ctx, key).Result()
	return result > 0, err
}

// CacheFlushPattern elimina todas las claves que coinciden con un patrón
func CacheFlushPattern(pattern string) error {
	iter := RedisClient.Scan(ctx, 0, pattern, 0).Iterator()
	for iter.Next(ctx) {
		if err := RedisClient.Del(ctx, iter.Val()).Err(); err != nil {
			return err
		}
	}
	return iter.Err()
}
