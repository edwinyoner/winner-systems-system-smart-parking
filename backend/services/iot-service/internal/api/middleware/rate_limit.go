package middleware

import (
	"context"
	"fmt"
	"net/http"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/redis/go-redis/v9"
)

// RateLimiter middleware para limitar tasa de requests
type RateLimiter struct {
	redisClient *redis.Client
	limit       int           // Número máximo de requests
	window      time.Duration // Ventana de tiempo
	keyPrefix   string        // Prefijo para keys de Redis
}

// RateLimiterConfig configuración del rate limiter
type RateLimiterConfig struct {
	RedisClient *redis.Client
	Limit       int           // Requests permitidos
	Window      time.Duration // Ventana de tiempo (ej: 1 minuto)
	KeyPrefix   string        // Prefijo opcional para keys
}

// NewRateLimiter crea una nueva instancia del rate limiter
func NewRateLimiter(config RateLimiterConfig) *RateLimiter {
	if config.KeyPrefix == "" {
		config.KeyPrefix = "ratelimit"
	}

	return &RateLimiter{
		redisClient: config.RedisClient,
		limit:       config.Limit,
		window:      config.Window,
		keyPrefix:   config.KeyPrefix,
	}
}

// Middleware retorna el middleware de rate limiting
func (rl *RateLimiter) Middleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		// Obtener identificador (IP del cliente por defecto)
		identifier := c.ClientIP()

		// Si existe X-Device-ID en headers, usar ese en su lugar
		if deviceID := c.GetHeader("X-Device-ID"); deviceID != "" {
			identifier = deviceID
		}

		// Verificar límite
		allowed, remaining, resetTime, err := rl.checkLimit(c.Request.Context(), identifier)
		if err != nil {
			// En caso de error con Redis, permitir el request (fail-open)
			c.Next()
			return
		}

		// Agregar headers de rate limit
		c.Header("X-RateLimit-Limit", strconv.Itoa(rl.limit))
		c.Header("X-RateLimit-Remaining", strconv.Itoa(remaining))
		c.Header("X-RateLimit-Reset", strconv.FormatInt(resetTime.Unix(), 10))

		if !allowed {
			c.JSON(http.StatusTooManyRequests, gin.H{
				"error":       "Rate limit exceeded",
				"message":     fmt.Sprintf("Límite de %d requests por %s excedido", rl.limit, rl.window),
				"retry_after": resetTime.Unix(),
			})
			c.Abort()
			return
		}

		c.Next()
	}
}

// checkLimit verifica si el identificador ha excedido el límite
func (rl *RateLimiter) checkLimit(ctx context.Context, identifier string) (bool, int, time.Time, error) {
	key := fmt.Sprintf("%s:%s", rl.keyPrefix, identifier)
	now := time.Now()
	windowStart := now.Add(-rl.window)

	// Pipeline para operaciones atómicas
	pipe := rl.redisClient.Pipeline()

	// Remover requests antiguos fuera de la ventana
	pipe.ZRemRangeByScore(ctx, key, "0", strconv.FormatInt(windowStart.UnixNano(), 10))

	// Contar requests en la ventana actual
	countCmd := pipe.ZCard(ctx, key)

	// Agregar el request actual
	pipe.ZAdd(ctx, key, redis.Z{
		Score:  float64(now.UnixNano()),
		Member: now.UnixNano(),
	})

	// Configurar expiración de la key
	pipe.Expire(ctx, key, rl.window)

	// Ejecutar pipeline
	_, err := pipe.Exec(ctx)
	if err != nil {
		return false, 0, time.Time{}, err
	}

	// Obtener cantidad de requests
	count := countCmd.Val()

	// Calcular remaining y reset time
	remaining := rl.limit - int(count)
	if remaining < 0 {
		remaining = 0
	}

	resetTime := now.Add(rl.window)

	// Verificar si está dentro del límite
	allowed := count < int64(rl.limit)

	return allowed, remaining, resetTime, nil
}

// DisplayRateLimiter rate limiter específico para displays LED (más restrictivo)
// 1 request cada 10 segundos por dispositivo
func DisplayRateLimiter(redisClient *redis.Client) gin.HandlerFunc {
	limiter := NewRateLimiter(RateLimiterConfig{
		RedisClient: redisClient,
		Limit:       1,
		Window:      10 * time.Second,
		KeyPrefix:   "ratelimit:display",
	})

	return limiter.Middleware()
}

// CaptureRateLimiter rate limiter para capturas de cámaras
// 10 requests por minuto por dispositivo
func CaptureRateLimiter(redisClient *redis.Client) gin.HandlerFunc {
	limiter := NewRateLimiter(RateLimiterConfig{
		RedisClient: redisClient,
		Limit:       10,
		Window:      1 * time.Minute,
		KeyPrefix:   "ratelimit:capture",
	})

	return limiter.Middleware()
}

// GlobalRateLimiter rate limiter general para la API
// 100 requests por minuto por IP
func GlobalRateLimiter(redisClient *redis.Client) gin.HandlerFunc {
	limiter := NewRateLimiter(RateLimiterConfig{
		RedisClient: redisClient,
		Limit:       100,
		Window:      1 * time.Minute,
		KeyPrefix:   "ratelimit:global",
	})

	return limiter.Middleware()
}

// BypassRateLimitForDevices middleware que permite bypassear rate limit para ciertos device IDs
func BypassRateLimitForDevices(whitelistedDevices []string) gin.HandlerFunc {
	// Crear mapa para búsqueda rápida
	whitelist := make(map[string]bool)
	for _, deviceID := range whitelistedDevices {
		whitelist[deviceID] = true
	}

	return func(c *gin.Context) {
		deviceID := c.GetHeader("X-Device-ID")
		if deviceID != "" && whitelist[deviceID] {
			// Marcar como whitelisted para que otros middlewares lo sepan
			c.Set("rate_limit_bypassed", true)
			c.Next()
			return
		}

		c.Next()
	}
}
