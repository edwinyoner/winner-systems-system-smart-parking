package models

import (
	"time"
)

// CaptureType representa el tipo de captura
type CaptureType string

const (
	CaptureTypeEntry CaptureType = "ENTRY"
	CaptureTypeExit  CaptureType = "EXIT"
)

// ProcessingStatus representa el estado del procesamiento
type ProcessingStatus string

const (
	ProcessingStatusPending    ProcessingStatus = "PENDING"
	ProcessingStatusProcessing ProcessingStatus = "PROCESSING"
	ProcessingStatusCompleted  ProcessingStatus = "COMPLETED"
	ProcessingStatusFailed     ProcessingStatus = "FAILED"
)

// CameraCapture representa una captura de ESP32-CAM frontal (entrada/salida)
type CameraCapture struct {
	ID               uint64           `gorm:"primaryKey;autoIncrement" json:"id"`
	CaptureID        string           `gorm:"type:varchar(50);uniqueIndex;not null" json:"captureId"`
	DeviceID         string           `gorm:"type:varchar(50);not null;index" json:"deviceId"`
	ParkingID        uint64           `gorm:"not null;index" json:"parkingId"`
	ImageURL         string           `gorm:"type:varchar(500);not null" json:"imageUrl"`
	CaptureType      CaptureType      `gorm:"type:enum('ENTRY','EXIT');not null;index" json:"captureType"`
	PlateNumber      *string          `gorm:"type:varchar(20)" json:"plateNumber,omitempty"`
	PlateConfidence  *float64         `gorm:"type:decimal(5,3)" json:"plateConfidence,omitempty"`
	TransactionID    *uint64          `json:"transactionId,omitempty"`
	ProcessingStatus ProcessingStatus `gorm:"type:enum('PENDING','PROCESSING','COMPLETED','FAILED');default:'PENDING';index" json:"processingStatus"`
	ErrorMessage     *string          `gorm:"type:text" json:"errorMessage,omitempty"`
	CreatedAt        time.Time        `gorm:"autoCreateTime;index" json:"createdAt"`
	ProcessedAt      *time.Time       `json:"processedAt,omitempty"`
}

// TableName especifica el nombre de la tabla en MySQL
func (CameraCapture) TableName() string {
	return "camera_captures"
}

// MarkAsProcessing marca la captura como en procesamiento
func (c *CameraCapture) MarkAsProcessing() {
	c.ProcessingStatus = ProcessingStatusProcessing
}

// MarkAsCompleted marca la captura como completada exitosamente
func (c *CameraCapture) MarkAsCompleted() {
	c.ProcessingStatus = ProcessingStatusCompleted
	now := time.Now()
	c.ProcessedAt = &now
}

// MarkAsFailed marca la captura como fallida con un mensaje de error
func (c *CameraCapture) MarkAsFailed(errorMsg string) {
	c.ProcessingStatus = ProcessingStatusFailed
	c.ErrorMessage = &errorMsg
	now := time.Now()
	c.ProcessedAt = &now
}

// IsEntry verifica si la captura es de entrada
func (c *CameraCapture) IsEntry() bool {
	return c.CaptureType == CaptureTypeEntry
}

// IsExit verifica si la captura es de salida
func (c *CameraCapture) IsExit() bool {
	return c.CaptureType == CaptureTypeExit
}
