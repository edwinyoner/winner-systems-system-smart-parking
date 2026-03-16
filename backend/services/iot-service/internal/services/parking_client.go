package services

import (
	"context"
	"fmt"

	"github.com/edwinyoner/winner-systems-system-smart-parking/backend/services/iot-service/pkg/httpclient"
)

// ParkingClient cliente para comunicarse con parking-service
type ParkingClient struct {
	client *httpclient.Client
}

// NewParkingClient crea una nueva instancia del cliente de parking
func NewParkingClient(baseURL string) *ParkingClient {
	return &ParkingClient{
		client: httpclient.NewClient(httpclient.Config{
			BaseURL:    baseURL,
			MaxRetries: 3,
		}),
	}
}

// ============================================================================
// TRANSACCIONES - ENTRADA (ESCENARIO_02)
// ============================================================================

// CreateEntryRequest estructura para crear entrada de vehículo
type CreateEntryRequest struct {
	ParkingID   uint64  `json:"parkingId"`
	ZoneID      uint64  `json:"zoneId,omitempty"`
	SpaceID     uint64  `json:"spaceId,omitempty"`
	PlateNumber string  `json:"plateNumber"`
	Confidence  float64 `json:"confidence"`
	ImageURL    string  `json:"imageUrl"`
	DeviceID    string  `json:"deviceId"`
	CaptureID   string  `json:"captureId"`
}

// CreateEntryResponse respuesta de creación de entrada
type CreateEntryResponse struct {
	Success       bool   `json:"success"`
	TransactionID uint64 `json:"transactionId"`
	CustomerID    uint64 `json:"customerId"`
	VehicleID     uint64 `json:"vehicleId"`
	SpaceCode     string `json:"spaceCode"`
	EntryTime     string `json:"entryTime"`
	Message       string `json:"message"`
	Error         string `json:"error,omitempty"`
}

// CreateEntry registra la entrada de un vehículo
// Endpoint: POST /api/v1/transactions/entry
func (c *ParkingClient) CreateEntry(ctx context.Context, req CreateEntryRequest) (*CreateEntryResponse, error) {
	// Agregar Authorization header con JWT (a implementar cuando se integre auth)
	headers := map[string]string{
		// "Authorization": "Bearer " + token,
	}

	resp, err := c.client.Post(ctx, "/api/v1/transactions/entry", req, headers)
	if err != nil {
		return nil, fmt.Errorf("error al llamar a parking-service/transactions/entry: %w", err)
	}

	if !resp.IsSuccess() {
		return nil, resp.GetError()
	}

	var result CreateEntryResponse
	if err := resp.DecodeJSON(&result); err != nil {
		return nil, fmt.Errorf("error al decodificar respuesta: %w", err)
	}

	return &result, nil
}

// ============================================================================
// ESPACIOS - ACTUALIZACIÓN MASIVA (ESCENARIO_01)
// ============================================================================

// BulkUpdateOccupancyRequest actualización masiva de ocupación
type BulkUpdateOccupancyRequest struct {
	ParkingID  uint64                 `json:"parkingId"`
	SnapshotID string                 `json:"snapshotId"`
	DeviceID   string                 `json:"deviceId"`
	Spaces     []SpaceOccupancyUpdate `json:"spaces"`
}

// SpaceOccupancyUpdate actualización de ocupación de un espacio
type SpaceOccupancyUpdate struct {
	SpaceCode   string  `json:"spaceCode"`
	IsOccupied  bool    `json:"isOccupied"`
	Confidence  float64 `json:"confidence"`
	PlateNumber string  `json:"plateNumber,omitempty"`
	ImageURL    string  `json:"imageUrl,omitempty"`
}

// BulkUpdateOccupancyResponse respuesta de actualización masiva
type BulkUpdateOccupancyResponse struct {
	Success        bool   `json:"success"`
	UpdatedSpaces  int    `json:"updatedSpaces"`
	OccupiedCount  int    `json:"occupiedCount"`
	AvailableCount int    `json:"availableCount"`
	Message        string `json:"message"`
	Error          string `json:"error,omitempty"`
}

// BulkUpdateOccupancy actualiza la ocupación de múltiples espacios
// Endpoint: PUT /api/v1/spaces/bulk-occupancy
func (c *ParkingClient) BulkUpdateOccupancy(ctx context.Context, req BulkUpdateOccupancyRequest) (*BulkUpdateOccupancyResponse, error) {
	headers := map[string]string{
		// "Authorization": "Bearer " + token,
	}

	resp, err := c.client.Put(ctx, "/api/v1/spaces/bulk-occupancy", req, headers)
	if err != nil {
		return nil, fmt.Errorf("error al llamar a parking-service/spaces/bulk-occupancy: %w", err)
	}

	if !resp.IsSuccess() {
		return nil, resp.GetError()
	}

	var result BulkUpdateOccupancyResponse
	if err := resp.DecodeJSON(&result); err != nil {
		return nil, fmt.Errorf("error al decodificar respuesta: %w", err)
	}

	return &result, nil
}

// ============================================================================
// CONSULTAS - DISPONIBILIDAD (Para Displays LED)
// ============================================================================

// GetAvailabilityRequest consulta de disponibilidad
type GetAvailabilityRequest struct {
	ParkingID uint64 `json:"parkingId"`
}

// GetAvailabilityResponse respuesta de disponibilidad
type GetAvailabilityResponse struct {
	ParkingID       uint64 `json:"parkingId"`
	TotalSpaces     int    `json:"totalSpaces"`
	OccupiedSpaces  int    `json:"occupiedSpaces"`
	AvailableSpaces int    `json:"availableSpaces"`
	LastUpdated     string `json:"lastUpdated"`
}

// GetAvailability obtiene la disponibilidad actual de un parking
// Endpoint: GET /api/v1/parkings/{parkingId}/availability
func (c *ParkingClient) GetAvailability(ctx context.Context, parkingID uint64) (*GetAvailabilityResponse, error) {
	path := fmt.Sprintf("/api/v1/parkings/%d/availability", parkingID)

	resp, err := c.client.Get(ctx, path, nil)
	if err != nil {
		return nil, fmt.Errorf("error al obtener disponibilidad: %w", err)
	}

	if !resp.IsSuccess() {
		return nil, resp.GetError()
	}

	var result GetAvailabilityResponse
	if err := resp.DecodeJSON(&result); err != nil {
		return nil, fmt.Errorf("error al decodificar respuesta: %w", err)
	}

	return &result, nil
}
