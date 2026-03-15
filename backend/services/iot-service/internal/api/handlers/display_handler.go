package handlers

import (
	"encoding/json"
	"fmt"
	"net/http"
	"strconv"
	"time"

	"github.com/edwinyoner/winner-systems-system-smart-parking/backend/services/iot-service/internal/models"
	"github.com/edwinyoner/winner-systems-system-smart-parking/backend/services/iot-service/internal/repository"
	"github.com/edwinyoner/winner-systems-system-smart-parking/backend/services/iot-service/internal/services"
	"github.com/gin-gonic/gin"
	"github.com/redis/go-redis/v9"
	"github.com/sirupsen/logrus"
)

// DisplayHandler maneja las operaciones de displays LED
type DisplayHandler struct {
	deviceRepo     *repository.DeviceRepository
	displayLogRepo *repository.DisplayLogRepository
	parkingClient  *services.ParkingClient
	redisClient    *redis.Client
	logger         *logrus.Logger
	cacheTTL       time.Duration
}

// NewDisplayHandler crea una nueva instancia del handler
func NewDisplayHandler(
	deviceRepo *repository.DeviceRepository,
	displayLogRepo *repository.DisplayLogRepository,
	parkingClient *services.ParkingClient,
	redisClient *redis.Client,
	logger *logrus.Logger,
) *DisplayHandler {
	return &DisplayHandler{
		deviceRepo:     deviceRepo,
		displayLogRepo: displayLogRepo,
		parkingClient:  parkingClient,
		redisClient:    redisClient,
		logger:         logger,
		cacheTTL:       30 * time.Second, // 30 segundos de cache
	}
}

// ============================================================================
// REQUEST/RESPONSE STRUCTS
// ============================================================================

// GetAvailabilityRequest solicitud de disponibilidad (desde ESP32 Display)
type GetAvailabilityRequest struct {
	DeviceID  string `json:"device_id" binding:"required"`
	ParkingID uint64 `json:"parking_id" binding:"required"`
}

// GetAvailabilityResponse respuesta de disponibilidad para display
type GetAvailabilityResponse struct {
	Success         bool   `json:"success"`
	ParkingID       uint64 `json:"parking_id"`
	TotalSpaces     int    `json:"total_spaces"`
	OccupiedSpaces  int    `json:"occupied_spaces"`
	AvailableSpaces int    `json:"available_spaces"`
	LastUpdated     string `json:"last_updated"`
	CacheHit        bool   `json:"cache_hit"`
	ResponseTimeMS  int64  `json:"response_time_ms"`
}

// ============================================================================
// ENDPOINTS
// ============================================================================

// GetAvailability maneja POST /api/v1/displays/availability
// Endpoint optimizado con Redis cache para consultas frecuentes de displays LED
func (h *DisplayHandler) GetAvailability(c *gin.Context) {
	startTime := time.Now()

	var req GetAvailabilityRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, ErrorResponse{
			Success: false,
			Error:   "Invalid request",
			Message: err.Error(),
		})
		return
	}

	// Verificar que el dispositivo existe
	device, err := h.deviceRepo.FindByDeviceID(req.DeviceID)
	if err != nil {
		c.JSON(http.StatusNotFound, ErrorResponse{
			Success: false,
			Error:   "Device not found",
			Message: "Dispositivo no encontrado",
		})
		return
	}

	// ✅ CORREGIDO: Validación simplificada - solo verificar que no sea CAMERA
	// (por ahora aceptamos cualquier tipo de dispositivo excepto CAMERA)
	if device.DeviceType == "CAMERA" {
		c.JSON(http.StatusForbidden, ErrorResponse{
			Success: false,
			Error:   "Invalid device type",
			Message: "Este endpoint no acepta dispositivos de tipo CAMERA",
		})
		return
	}

	// Actualizar heartbeat del dispositivo
	go h.deviceRepo.UpdateHeartbeat(req.DeviceID)

	// Intentar obtener de cache
	cacheKey := fmt.Sprintf("availability:parking:%d", req.ParkingID)
	cachedData, err := h.redisClient.Get(c.Request.Context(), cacheKey).Result()

	var response GetAvailabilityResponse

	if err == nil && cachedData != "" {
		// Cache hit
		if err := json.Unmarshal([]byte(cachedData), &response); err == nil {
			response.CacheHit = true
			response.ResponseTimeMS = time.Since(startTime).Milliseconds()

			// Registrar log de display (cache hit)
			go h.logDisplayQuery(req.DeviceID, req.ParkingID, response)

			c.JSON(http.StatusOK, response)
			return
		}
	}

	// Cache miss - consultar parking-service
	availability, err := h.parkingClient.GetAvailability(c.Request.Context(), req.ParkingID)
	if err != nil {
		h.logger.WithError(err).Error("Error al obtener disponibilidad de parking-service")
		c.JSON(http.StatusInternalServerError, ErrorResponse{
			Success: false,
			Error:   "Service unavailable",
			Message: "Error al obtener disponibilidad",
		})
		return
	}

	// Construir respuesta
	response = GetAvailabilityResponse{
		Success:         true,
		ParkingID:       availability.ParkingID,
		TotalSpaces:     availability.TotalSpaces,
		OccupiedSpaces:  availability.OccupiedSpaces,
		AvailableSpaces: availability.AvailableSpaces,
		LastUpdated:     availability.LastUpdated,
		CacheHit:        false,
		ResponseTimeMS:  time.Since(startTime).Milliseconds(),
	}

	// Guardar en cache
	responseJSON, _ := json.Marshal(response)
	h.redisClient.Set(c.Request.Context(), cacheKey, responseJSON, h.cacheTTL)

	// Registrar log de display (cache miss)
	go h.logDisplayQuery(req.DeviceID, req.ParkingID, response)

	c.JSON(http.StatusOK, response)
}

// logDisplayQuery registra la consulta en display_logs
func (h *DisplayHandler) logDisplayQuery(deviceID string, parkingID uint64, response GetAvailabilityResponse) {
	// ✅ CORREGIDO: Convertir int64 a *int
	responseTimeMs := int(response.ResponseTimeMS)

	log := &models.DisplayLog{
		DeviceID:        deviceID,
		ParkingID:       parkingID,
		AvailableSpaces: response.AvailableSpaces,
		OccupiedSpaces:  response.OccupiedSpaces,
		ResponseTimeMs:  &responseTimeMs, // ✅ Puntero a int
	}

	if err := h.displayLogRepo.Create(log); err != nil {
		h.logger.WithError(err).Error("Error al crear display log")
	}
}

// GetDisplayAnalytics maneja GET /api/v1/displays/analytics/:deviceId
func (h *DisplayHandler) GetDisplayAnalytics(c *gin.Context) {
	deviceID := c.Param("deviceId")

	// Parámetro opcional: hours (por defecto 24)
	hoursStr := c.DefaultQuery("hours", "24")
	hours, err := strconv.Atoi(hoursStr)
	if err != nil || hours <= 0 {
		hours = 24
	}

	since := time.Now().Add(-time.Duration(hours) * time.Hour)

	analytics, err := h.displayLogRepo.GetAnalyticsByDevice(deviceID, since)
	if err != nil {
		c.JSON(http.StatusInternalServerError, ErrorResponse{
			Success: false,
			Error:   "Database error",
			Message: "Error al obtener analytics",
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"success":      true,
		"device_id":    deviceID,
		"period_hours": hours,
		"analytics":    analytics,
	})
}

// GetParkingAnalytics maneja GET /api/v1/displays/analytics/parking/:parkingId
func (h *DisplayHandler) GetParkingAnalytics(c *gin.Context) {
	parkingIDStr := c.Param("parkingId")
	parkingID, err := strconv.ParseUint(parkingIDStr, 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, ErrorResponse{
			Success: false,
			Error:   "Invalid parking ID",
			Message: "ID de parking inválido",
		})
		return
	}

	// Parámetro opcional: hours (por defecto 24)
	hoursStr := c.DefaultQuery("hours", "24")
	hours, err := strconv.Atoi(hoursStr)
	if err != nil || hours <= 0 {
		hours = 24
	}

	since := time.Now().Add(-time.Duration(hours) * time.Hour)

	analytics, err := h.displayLogRepo.GetAnalyticsByParkingID(parkingID, since)
	if err != nil {
		c.JSON(http.StatusInternalServerError, ErrorResponse{
			Success: false,
			Error:   "Database error",
			Message: "Error al obtener analytics",
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"success":      true,
		"parking_id":   parkingID,
		"period_hours": hours,
		"analytics":    analytics,
	})
}

// InvalidateCache maneja DELETE /api/v1/displays/cache/:parkingId
// Endpoint administrativo para invalidar cache manualmente
func (h *DisplayHandler) InvalidateCache(c *gin.Context) {
	parkingIDStr := c.Param("parkingId")
	parkingID, err := strconv.ParseUint(parkingIDStr, 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, ErrorResponse{
			Success: false,
			Error:   "Invalid parking ID",
			Message: "ID de parking inválido",
		})
		return
	}

	cacheKey := fmt.Sprintf("availability:parking:%d", parkingID)
	err = h.redisClient.Del(c.Request.Context(), cacheKey).Err()
	if err != nil {
		c.JSON(http.StatusInternalServerError, ErrorResponse{
			Success: false,
			Error:   "Cache error",
			Message: "Error al invalidar cache",
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"success":    true,
		"message":    "Cache invalidado exitosamente",
		"parking_id": parkingID,
	})
}
