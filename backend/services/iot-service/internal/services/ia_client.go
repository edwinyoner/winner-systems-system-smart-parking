package services

import (
	"context"
	"fmt"

	"github.com/edwinyoner/winner-systems-system-smart-parking/backend/services/iot-service/pkg/httpclient"
)

// IaClient cliente para comunicarse con ia-service
type IaClient struct {
	client *httpclient.Client
}

// NewIaClient crea una nueva instancia del cliente de IA
func NewIaClient(baseURL string) *IaClient {
	return &IaClient{
		client: httpclient.NewClient(httpclient.Config{
			BaseURL:    baseURL,
			MaxRetries: 3,
		}),
	}
}

// ============================================================================
// RECONOCIMIENTO DE PLACAS (Entrada/Salida - ESCENARIO_02)
// ============================================================================

// PlateRecognitionRequest estructura para solicitud de reconocimiento de placas
type PlateRecognitionRequest struct {
	ImageBase64 string `json:"image_base64"` // Imagen en base64
	DeviceID    string `json:"device_id"`    // ID del dispositivo (opcional para logging)
}

// PlateRecognitionResponse estructura de respuesta del reconocimiento
type PlateRecognitionResponse struct {
	Success      bool    `json:"success"`
	PlateNumber  string  `json:"plate_number"`
	Confidence   float64 `json:"confidence"`
	ProcessingMS int     `json:"processing_ms"`
	Error        string  `json:"error,omitempty"`
}

// RecognizePlate llama al endpoint de reconocimiento de placas
// Endpoint: POST /api/v1/recognitions
func (c *IaClient) RecognizePlate(ctx context.Context, req PlateRecognitionRequest) (*PlateRecognitionResponse, error) {
	resp, err := c.client.Post(ctx, "/api/v1/recognitions", req, nil)
	if err != nil {
		return nil, fmt.Errorf("error al llamar a ia-service/recognitions: %w", err)
	}

	if !resp.IsSuccess() {
		return nil, resp.GetError()
	}

	var result PlateRecognitionResponse
	if err := resp.DecodeJSON(&result); err != nil {
		return nil, fmt.Errorf("error al decodificar respuesta: %w", err)
	}

	return &result, nil
}

// ============================================================================
// PARKING VISION (Vista Aérea - ESCENARIO_01)
// ============================================================================

// ParkingVisionRequest estructura para detección de ocupación aérea
type ParkingVisionRequest struct {
	ImageBase64   string              `json:"image_base64"`   // Imagen aérea en base64
	ParkingID     uint64              `json:"parking_id"`     // ID del parking
	DeviceID      string              `json:"device_id"`      // ID del dispositivo
	SpacePolygons []SpacePolygonInput `json:"space_polygons"` // Polígonos de espacios
}

// SpacePolygonInput define un polígono de espacio de estacionamiento
type SpacePolygonInput struct {
	SpaceCode string      `json:"space_code"` // Código del espacio (ej: "A-001")
	Polygon   [][]float64 `json:"polygon"`    // Array de puntos [[x1,y1], [x2,y2], ...]
}

// ParkingVisionResponse respuesta de detección de ocupación
type ParkingVisionResponse struct {
	Success         bool                   `json:"success"`
	TotalSpaces     int                    `json:"total_spaces"`
	OccupiedSpaces  int                    `json:"occupied_spaces"`
	AvailableSpaces int                    `json:"available_spaces"`
	Detections      []SpaceDetectionResult `json:"detections"`
	ProcessingMS    int                    `json:"processing_ms"`
	Error           string                 `json:"error,omitempty"`
}

// SpaceDetectionResult resultado de detección por espacio
type SpaceDetectionResult struct {
	SpaceCode       string  `json:"space_code"`
	IsOccupied      bool    `json:"is_occupied"`
	Confidence      float64 `json:"confidence"`
	PlateNumber     string  `json:"plate_number,omitempty"` // Solo si está ocupado y se detectó placa
	PlateConfidence float64 `json:"plate_confidence,omitempty"`
}

// DetectOccupancy llama al endpoint de detección de ocupación aérea
// Endpoint: POST /api/v1/parking-vision
func (c *IaClient) DetectOccupancy(ctx context.Context, req ParkingVisionRequest) (*ParkingVisionResponse, error) {
	resp, err := c.client.Post(ctx, "/api/v1/parking-vision", req, nil)
	if err != nil {
		return nil, fmt.Errorf("error al llamar a ia-service/parking-vision: %w", err)
	}

	if !resp.IsSuccess() {
		return nil, resp.GetError()
	}

	var result ParkingVisionResponse
	if err := resp.DecodeJSON(&result); err != nil {
		return nil, fmt.Errorf("error al decodificar respuesta: %w", err)
	}

	return &result, nil
}

// ============================================================================
// HEALTH CHECKS
// ============================================================================

// HealthResponse respuesta de health check
type HealthResponse struct {
	Status  string `json:"status"`
	Service string `json:"service"`
	Version string `json:"version,omitempty"`
}

// CheckHealth verifica el estado del servicio de IA
// Endpoint: GET /api/v1/recognitions/status
func (c *IaClient) CheckHealth(ctx context.Context) (*HealthResponse, error) {
	resp, err := c.client.Get(ctx, "/api/v1/recognitions/status", nil)
	if err != nil {
		return nil, fmt.Errorf("error al verificar health de ia-service: %w", err)
	}

	if !resp.IsSuccess() {
		return nil, resp.GetError()
	}

	var result HealthResponse
	if err := resp.DecodeJSON(&result); err != nil {
		return nil, fmt.Errorf("error al decodificar respuesta: %w", err)
	}

	return &result, nil
}

// CheckParkingVisionHealth verifica el estado del módulo parking-vision
// Endpoint: GET /api/v1/parking-vision/health
func (c *IaClient) CheckParkingVisionHealth(ctx context.Context) (*HealthResponse, error) {
	resp, err := c.client.Get(ctx, "/api/v1/parking-vision/health", nil)
	if err != nil {
		return nil, fmt.Errorf("error al verificar health de parking-vision: %w", err)
	}

	if !resp.IsSuccess() {
		return nil, resp.GetError()
	}

	var result HealthResponse
	if err := resp.DecodeJSON(&result); err != nil {
		return nil, fmt.Errorf("error al decodificar respuesta: %w", err)
	}

	return &result, nil
}
