package handlers

import (
	"context"
	"encoding/base64"
	"fmt"
	"net/http"

	"github.com/edwinyoner/winner-systems-system-smart-parking/backend/services/iot-service/internal/models"
	"github.com/edwinyoner/winner-systems-system-smart-parking/backend/services/iot-service/internal/repository"
	"github.com/edwinyoner/winner-systems-system-smart-parking/backend/services/iot-service/internal/services"
	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"github.com/sirupsen/logrus"
)

// CaptureHandler maneja las operaciones de capturas de cámaras
type CaptureHandler struct {
	deviceRepo    *repository.DeviceRepository
	captureRepo   *repository.CaptureRepository
	iaClient      *services.IaClient
	parkingClient *services.ParkingClient
	s3Service     *services.S3Service
	logger        *logrus.Logger
}

// NewCaptureHandler crea una nueva instancia del handler
func NewCaptureHandler(
	deviceRepo *repository.DeviceRepository,
	captureRepo *repository.CaptureRepository,
	iaClient *services.IaClient,
	parkingClient *services.ParkingClient,
	s3Service *services.S3Service,
	logger *logrus.Logger,
) *CaptureHandler {
	return &CaptureHandler{
		deviceRepo:    deviceRepo,
		captureRepo:   captureRepo,
		iaClient:      iaClient,
		parkingClient: parkingClient,
		s3Service:     s3Service,
		logger:        logger,
	}
}

// ============================================================================
// REQUEST/RESPONSE STRUCTS
// ============================================================================

// CreateCaptureRequest solicitud para crear una captura (desde ESP32-CAM)
type CreateCaptureRequest struct {
	DeviceID    string `json:"device_id" binding:"required"`
	ParkingID   uint64 `json:"parking_id" binding:"required"`
	CaptureType string `json:"capture_type" binding:"required"` // "entry" o "exit"
	ImageBase64 string `json:"image_base64" binding:"required"`
	Timestamp   string `json:"timestamp,omitempty"`
}

// CreateCaptureResponse respuesta de creación de captura
type CreateCaptureResponse struct {
	Success          bool    `json:"success"`
	CaptureID        string  `json:"capture_id"`
	Message          string  `json:"message"`
	ProcessingStatus string  `json:"processing_status"`
	TransactionID    uint64  `json:"transaction_id,omitempty"`
	PlateNumber      string  `json:"plate_number,omitempty"`
	Confidence       float64 `json:"confidence,omitempty"`
}

// ErrorResponse respuesta de error estándar
type ErrorResponse struct {
	Success bool   `json:"success"`
	Error   string `json:"error"`
	Message string `json:"message"`
}

// ============================================================================
// ENDPOINTS
// ============================================================================

// CreateCapture maneja POST /api/v1/captures
// Flujo completo: ESP32-CAM -> iot-service -> ia-service -> parking-service
func (h *CaptureHandler) CreateCapture(c *gin.Context) {
	var req CreateCaptureRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		h.logger.WithError(err).Error("Error al parsear request")
		c.JSON(http.StatusBadRequest, ErrorResponse{
			Success: false,
			Error:   "Invalid request",
			Message: err.Error(),
		})
		return
	}

	// Validar tipo de captura
	if req.CaptureType != "entry" && req.CaptureType != "exit" {
		c.JSON(http.StatusBadRequest, ErrorResponse{
			Success: false,
			Error:   "Invalid capture type",
			Message: "capture_type debe ser 'entry' o 'exit'",
		})
		return
	}

	// Verificar que el dispositivo existe y está activo
	device, err := h.deviceRepo.FindByDeviceID(req.DeviceID)
	if err != nil {
		h.logger.WithError(err).WithField("device_id", req.DeviceID).Error("Dispositivo no encontrado")
		c.JSON(http.StatusNotFound, ErrorResponse{
			Success: false,
			Error:   "Device not found",
			Message: "El dispositivo especificado no existe",
		})
		return
	}

	if device.Status != models.DeviceStatusActive {
		c.JSON(http.StatusForbidden, ErrorResponse{
			Success: false,
			Error:   "Device inactive",
			Message: "El dispositivo no está activo",
		})
		return
	}

	// Generar CaptureID único
	captureID := uuid.New().String()

	// Decodificar imagen base64
	imageData, err := base64.StdEncoding.DecodeString(req.ImageBase64)
	if err != nil {
		h.logger.WithError(err).Error("Error al decodificar imagen base64")
		c.JSON(http.StatusBadRequest, ErrorResponse{
			Success: false,
			Error:   "Invalid image",
			Message: "Error al decodificar imagen base64",
		})
		return
	}

	// Subir imagen a S3
	objectName := services.GenerateObjectName("captures", req.DeviceID, "jpg")
	imageURL, err := h.s3Service.UploadImage(c.Request.Context(), objectName, imageData, "image/jpeg")
	if err != nil {
		h.logger.WithError(err).Error("Error al subir imagen a S3")
		c.JSON(http.StatusInternalServerError, ErrorResponse{
			Success: false,
			Error:   "Storage error",
			Message: "Error al almacenar imagen",
		})
		return
	}

	// Crear registro de captura en base de datos
	capture := &models.CameraCapture{
		CaptureID:        captureID,
		DeviceID:         req.DeviceID,
		ParkingID:        req.ParkingID,
		CaptureType:      models.CaptureType(req.CaptureType), // ✅ CORREGIDO: Cast a models.CaptureType
		ImageURL:         imageURL,
		ProcessingStatus: models.ProcessingStatusPending,
	}

	if err := h.captureRepo.Create(capture); err != nil {
		h.logger.WithError(err).Error("Error al crear captura en DB")
		c.JSON(http.StatusInternalServerError, ErrorResponse{
			Success: false,
			Error:   "Database error",
			Message: "Error al registrar captura",
		})
		return
	}

	// Procesar captura de forma asíncrona
	go h.processCaptureAsync(captureID, req.ImageBase64, imageURL)

	// Retornar respuesta inmediata
	c.JSON(http.StatusAccepted, CreateCaptureResponse{
		Success:          true,
		CaptureID:        captureID,
		Message:          "Captura recibida, procesando...",
		ProcessingStatus: string(models.ProcessingStatusPending),
	})
}

// processCaptureAsync procesa la captura de forma asíncrona
func (h *CaptureHandler) processCaptureAsync(captureID, imageBase64, imageURL string) {
	ctx := context.Background()

	// Marcar como procesando
	if err := h.captureRepo.MarkAsProcessing(captureID); err != nil {
		h.logger.WithError(err).Error("Error al marcar captura como procesando")
		return
	}

	// Obtener captura de DB
	capture, err := h.captureRepo.FindByCaptureID(captureID)
	if err != nil {
		h.logger.WithError(err).Error("Error al obtener captura")
		h.captureRepo.MarkAsFailed(captureID, "Error al obtener captura de DB")
		return
	}

	// Llamar a ia-service para reconocimiento de placa
	iaReq := services.PlateRecognitionRequest{
		ImageBase64: imageBase64,
		DeviceID:    capture.DeviceID,
	}

	iaResp, err := h.iaClient.RecognizePlate(ctx, iaReq)
	if err != nil || !iaResp.Success {
		errMsg := "Error al reconocer placa"
		if err != nil {
			errMsg = err.Error()
		} else if iaResp.Error != "" {
			errMsg = iaResp.Error
		}

		h.logger.WithError(err).Error("Error en ia-service")
		h.captureRepo.MarkAsFailed(captureID, errMsg)
		return
	}

	// Si la confianza es muy baja, marcar como fallido
	if iaResp.Confidence < 0.5 {
		h.captureRepo.MarkAsFailed(captureID, fmt.Sprintf("Confianza muy baja: %.2f%%", iaResp.Confidence*100))
		return
	}

	// Llamar a parking-service para crear entrada
	parkingReq := services.CreateEntryRequest{
		ParkingID:   capture.ParkingID,
		PlateNumber: iaResp.PlateNumber,
		Confidence:  iaResp.Confidence,
		ImageURL:    imageURL,
		DeviceID:    capture.DeviceID,
		CaptureID:   captureID,
	}

	parkingResp, err := h.parkingClient.CreateEntry(ctx, parkingReq)
	if err != nil || !parkingResp.Success {
		errMsg := "Error al crear entrada en parking-service"
		if err != nil {
			errMsg = err.Error()
		} else if parkingResp.Error != "" {
			errMsg = parkingResp.Error
		}

		h.logger.WithError(err).Error("Error en parking-service")
		h.captureRepo.MarkAsFailed(captureID, errMsg)
		return
	}

	// Marcar captura como completada
	err = h.captureRepo.MarkAsCompleted(
		captureID,
		iaResp.PlateNumber,
		iaResp.Confidence,
		parkingResp.TransactionID,
	)

	if err != nil {
		h.logger.WithError(err).Error("Error al marcar captura como completada")
		return
	}

	h.logger.WithFields(logrus.Fields{
		"capture_id":     captureID,
		"plate_number":   iaResp.PlateNumber,
		"confidence":     iaResp.Confidence,
		"transaction_id": parkingResp.TransactionID,
	}).Info("Captura procesada exitosamente")
}

// GetCaptureStatus maneja GET /api/v1/captures/:captureId/status
func (h *CaptureHandler) GetCaptureStatus(c *gin.Context) {
	captureID := c.Param("captureId")

	capture, err := h.captureRepo.FindByCaptureID(captureID)
	if err != nil {
		c.JSON(http.StatusNotFound, ErrorResponse{
			Success: false,
			Error:   "Capture not found",
			Message: "Captura no encontrada",
		})
		return
	}

	response := CreateCaptureResponse{
		Success:          true,
		CaptureID:        capture.CaptureID,
		ProcessingStatus: string(capture.ProcessingStatus),
	}

	if capture.ProcessingStatus == models.ProcessingStatusCompleted {
		// ✅ CORREGIDO: Dereferencia punteros con verificación de nil
		if capture.TransactionID != nil {
			response.TransactionID = *capture.TransactionID
		}
		if capture.PlateNumber != nil {
			response.PlateNumber = *capture.PlateNumber
		}
		if capture.PlateConfidence != nil {
			response.Confidence = *capture.PlateConfidence
		}
		response.Message = "Captura procesada exitosamente"
	} else if capture.ProcessingStatus == models.ProcessingStatusFailed {
		// ✅ CORREGIDO: Dereferencia puntero con verificación de nil
		if capture.ErrorMessage != nil {
			response.Message = *capture.ErrorMessage
		} else {
			response.Message = "Error desconocido"
		}
	} else {
		response.Message = "Procesando..."
	}

	c.JSON(http.StatusOK, response)
}

// GetCapturesByDevice maneja GET /api/v1/captures/device/:deviceId
func (h *CaptureHandler) GetCapturesByDevice(c *gin.Context) {
	deviceID := c.Param("deviceId")
	limit := 10 // Por defecto

	captures, err := h.captureRepo.FindByDeviceID(deviceID, limit)
	if err != nil {
		c.JSON(http.StatusInternalServerError, ErrorResponse{
			Success: false,
			Error:   "Database error",
			Message: "Error al obtener capturas",
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"success":  true,
		"captures": captures,
		"total":    len(captures),
	})
}
