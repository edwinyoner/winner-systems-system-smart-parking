package handlers

import (
	"context"
	"net/http"
	"time"

	"github.com/edwinyoner/winner-systems-system-smart-parking/backend/services/iot-service/internal/services"
	"github.com/gin-gonic/gin"
	"github.com/redis/go-redis/v9"
	"github.com/sirupsen/logrus"
	"gorm.io/gorm"
)

// HealthHandler maneja los health checks del servicio
type HealthHandler struct {
	db            *gorm.DB
	redisClient   *redis.Client
	iaClient      *services.IaClient
	parkingClient *services.ParkingClient
	logger        *logrus.Logger
	startTime     time.Time
}

// NewHealthHandler crea una nueva instancia del handler
func NewHealthHandler(
	db *gorm.DB,
	redisClient *redis.Client,
	iaClient *services.IaClient,
	parkingClient *services.ParkingClient,
	logger *logrus.Logger,
) *HealthHandler {
	return &HealthHandler{
		db:            db,
		redisClient:   redisClient,
		iaClient:      iaClient,
		parkingClient: parkingClient,
		logger:        logger,
		startTime:     time.Now(),
	}
}

// ============================================================================
// RESPONSE STRUCTS
// ============================================================================

// HealthResponse respuesta básica de health
type HealthResponse struct {
	Status  string `json:"status"`
	Service string `json:"service"`
	Version string `json:"version"`
	Uptime  string `json:"uptime"`
}

// DetailedHealthResponse respuesta detallada de health
type DetailedHealthResponse struct {
	Status       string               `json:"status"`
	Service      string               `json:"service"`
	Version      string               `json:"version"`
	Uptime       string               `json:"uptime"`
	Timestamp    string               `json:"timestamp"`
	Dependencies map[string]DepHealth `json:"dependencies"`
}

// DepHealth estado de una dependencia
type DepHealth struct {
	Status       string `json:"status"`
	ResponseTime string `json:"response_time,omitempty"`
	Error        string `json:"error,omitempty"`
}

// ============================================================================
// ENDPOINTS
// ============================================================================

// Health maneja GET /health - Health check básico
func (h *HealthHandler) Health(c *gin.Context) {
	uptime := time.Since(h.startTime)

	c.JSON(http.StatusOK, HealthResponse{
		Status:  "healthy",
		Service: "iot-service",
		Version: "1.0.0",
		Uptime:  uptime.String(),
	})
}

// Ready maneja GET /ready - Readiness probe (verifica dependencias)
func (h *HealthHandler) Ready(c *gin.Context) {
	ctx, cancel := context.WithTimeout(c.Request.Context(), 5*time.Second)
	defer cancel()

	dependencies := make(map[string]DepHealth)
	allHealthy := true

	// Verificar MySQL
	mysqlStart := time.Now()
	sqlDB, err := h.db.DB()
	if err != nil {
		dependencies["mysql"] = DepHealth{
			Status: "unhealthy",
			Error:  err.Error(),
		}
		allHealthy = false
	} else {
		err = sqlDB.PingContext(ctx)
		if err != nil {
			dependencies["mysql"] = DepHealth{
				Status: "unhealthy",
				Error:  err.Error(),
			}
			allHealthy = false
		} else {
			dependencies["mysql"] = DepHealth{
				Status:       "healthy",
				ResponseTime: time.Since(mysqlStart).String(),
			}
		}
	}

	// Verificar Redis
	redisStart := time.Now()
	err = h.redisClient.Ping(ctx).Err()
	if err != nil {
		dependencies["redis"] = DepHealth{
			Status: "unhealthy",
			Error:  err.Error(),
		}
		allHealthy = false
	} else {
		dependencies["redis"] = DepHealth{
			Status:       "healthy",
			ResponseTime: time.Since(redisStart).String(),
		}
	}

	// Verificar ia-service
	iaStart := time.Now()
	_, err = h.iaClient.CheckHealth(ctx)
	if err != nil {
		dependencies["ia-service"] = DepHealth{
			Status: "unhealthy",
			Error:  err.Error(),
		}
		allHealthy = false
	} else {
		dependencies["ia-service"] = DepHealth{
			Status:       "healthy",
			ResponseTime: time.Since(iaStart).String(),
		}
	}

	// Determinar status general
	status := "healthy"
	statusCode := http.StatusOK
	if !allHealthy {
		status = "degraded"
		statusCode = http.StatusServiceUnavailable
	}

	uptime := time.Since(h.startTime)

	response := DetailedHealthResponse{
		Status:       status,
		Service:      "iot-service",
		Version:      "1.0.0",
		Uptime:       uptime.String(),
		Timestamp:    time.Now().Format(time.RFC3339),
		Dependencies: dependencies,
	}

	c.JSON(statusCode, response)
}

// Live maneja GET /live - Liveness probe (solo verifica que el servicio responde)
func (h *HealthHandler) Live(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{
		"status":  "alive",
		"service": "iot-service",
	})
}

// Metrics maneja GET /metrics - Métricas básicas del servicio
func (h *HealthHandler) Metrics(c *gin.Context) {
	ctx := c.Request.Context()

	// Obtener estadísticas de MySQL
	sqlDB, _ := h.db.DB()
	dbStats := sqlDB.Stats()

	// Obtener estadísticas de Redis
	// ✅ CORREGIDO: Usar _ para variable no usada
	_, err := h.redisClient.Info(ctx, "stats").Result()
	redisStats := map[string]interface{}{
		"available": err == nil,
	}
	if err == nil {
		redisStats["info"] = "available"
	}

	uptime := time.Since(h.startTime)

	c.JSON(http.StatusOK, gin.H{
		"service":        "iot-service",
		"uptime_seconds": uptime.Seconds(),
		"database": gin.H{
			"max_open_connections": dbStats.MaxOpenConnections,
			"open_connections":     dbStats.OpenConnections,
			"in_use":               dbStats.InUse,
			"idle":                 dbStats.Idle,
		},
		"redis":     redisStats,
		"timestamp": time.Now().Format(time.RFC3339),
	})
}
