package api

import (
	"github.com/edwinyoner/winner-systems-system-smart-parking/backend/services/iot-service/internal/api/handlers"
	"github.com/edwinyoner/winner-systems-system-smart-parking/backend/services/iot-service/internal/api/middleware"
	"github.com/gin-gonic/gin"
	"github.com/redis/go-redis/v9"
	"github.com/sirupsen/logrus"
)

// RouterConfig configuración del router
type RouterConfig struct {
	CaptureHandler *handlers.CaptureHandler
	DisplayHandler *handlers.DisplayHandler
	HealthHandler  *handlers.HealthHandler
	RedisClient    *redis.Client
	Logger         *logrus.Logger
	Environment    string // "development" o "production"
}

// SetupRouter configura y retorna el router Gin
func SetupRouter(config RouterConfig) *gin.Engine {
	// Configurar modo Gin según environment
	if config.Environment == "production" {
		gin.SetMode(gin.ReleaseMode)
	}

	// Crear router
	router := gin.New()

	// ========================================================================
	// MIDDLEWARES GLOBALES
	// ========================================================================

	// Recovery middleware (debe ser primero)
	router.Use(gin.Recovery())

	// Error logger middleware
	router.Use(middleware.ErrorLogger(config.Logger))

	// Request ID middleware
	router.Use(middleware.RequestID())

	// Logger middleware (con configuración personalizada)
	router.Use(middleware.LoggerWithConfig(middleware.LoggerConfig{
		Logger: config.Logger,
		SkipPaths: []string{
			"/health",
			"/live",
		},
		LogLatency:   true,
		LogUserAgent: false,
	}))

	// CORS middleware
	if config.Environment == "development" {
		router.Use(middleware.CORSDefault())
	} else {
		// En producción, configurar origins específicos
		router.Use(middleware.CORS(middleware.ProductionCORSConfig([]string{
			"https://smart-parking.winner-systems.com",
			"https://admin.winner-systems.com",
		})))
	}

	// Rate limiting global (100 req/min por IP)
	router.Use(middleware.GlobalRateLimiter(config.RedisClient))

	// ========================================================================
	// HEALTH CHECK ROUTES (sin autenticación)
	// ========================================================================

	router.GET("/health", config.HealthHandler.Health)
	router.GET("/ready", config.HealthHandler.Ready)
	router.GET("/live", config.HealthHandler.Live)
	router.GET("/metrics", config.HealthHandler.Metrics)

	// ========================================================================
	// API ROUTES v1
	// ========================================================================

	v1 := router.Group("/api/v1")
	{
		// ====================================================================
		// CAPTURES ROUTES (ESP32-CAM)
		// ====================================================================
		captures := v1.Group("/captures")
		{
			// Rate limiting específico para capturas (10 req/min por dispositivo)
			captures.Use(middleware.CaptureRateLimiter(config.RedisClient))

			captures.POST("", config.CaptureHandler.CreateCapture)
			captures.GET("/:captureId/status", config.CaptureHandler.GetCaptureStatus)
			captures.GET("/device/:deviceId", config.CaptureHandler.GetCapturesByDevice)
		}

		// ====================================================================
		// DISPLAYS ROUTES (ESP32 LED)
		// ====================================================================
		displays := v1.Group("/displays")
		{
			// Rate limiting específico para displays (1 req/10s por dispositivo)
			displays.POST("/availability",
				middleware.DisplayRateLimiter(config.RedisClient),
				config.DisplayHandler.GetAvailability,
			)

			// Analytics sin rate limiting estricto
			displays.GET("/analytics/:deviceId", config.DisplayHandler.GetDisplayAnalytics)
			displays.GET("/analytics/parking/:parkingId", config.DisplayHandler.GetParkingAnalytics)

			// Administrativo - invalidar cache
			displays.DELETE("/cache/:parkingId", config.DisplayHandler.InvalidateCache)
		}

		// ====================================================================
		// SNAPSHOTS ROUTES (Vista Aérea - ESCENARIO_01)
		// ====================================================================
		// TODO: Implementar en siguiente fase
		// snapshots := v1.Group("/snapshots")
		// {
		//     snapshots.POST("", snapshotHandler.CreateSnapshot)
		//     snapshots.GET("/:snapshotId/status", snapshotHandler.GetStatus)
		//     snapshots.GET("/parking/:parkingId/latest", snapshotHandler.GetLatest)
		// }

		// ====================================================================
		// DEVICES ROUTES (Gestión de dispositivos)
		// ====================================================================
		// TODO: Implementar en siguiente fase
		// devices := v1.Group("/devices")
		// {
		//     devices.POST("", deviceHandler.RegisterDevice)
		//     devices.GET("/:deviceId", deviceHandler.GetDevice)
		//     devices.PUT("/:deviceId", deviceHandler.UpdateDevice)
		//     devices.POST("/:deviceId/heartbeat", deviceHandler.Heartbeat)
		// }
	}

	// ========================================================================
	// 404 HANDLER
	// ========================================================================
	router.NoRoute(func(c *gin.Context) {
		c.JSON(404, gin.H{
			"success": false,
			"error":   "Not Found",
			"message": "El endpoint solicitado no existe",
			"path":    c.Request.URL.Path,
		})
	})

	return router
}
