package models

import (
	"time"
)

// ParkingSnapshot representa un snapshot de vista aérea completa del parking
type ParkingSnapshot struct {
	ID               uint64           `gorm:"primaryKey;autoIncrement" json:"id"`
	SnapshotID       string           `gorm:"type:varchar(50);uniqueIndex;not null" json:"snapshotId"`
	DeviceID         string           `gorm:"type:varchar(50);not null;index" json:"deviceId"`
	ParkingID        uint64           `gorm:"not null;index" json:"parkingId"`
	ImageURL         string           `gorm:"type:varchar(500);not null" json:"imageUrl"`
	TotalSpaces      int              `gorm:"not null" json:"totalSpaces"`
	OccupiedSpaces   int              `gorm:"not null" json:"occupiedSpaces"`
	AvailableSpaces  int              `gorm:"not null" json:"availableSpaces"`
	ProcessingStatus ProcessingStatus `gorm:"type:enum('PENDING','PROCESSING','COMPLETED','FAILED');default:'PENDING';index" json:"processingStatus"`
	ErrorMessage     *string          `gorm:"type:text" json:"errorMessage,omitempty"`
	CreatedAt        time.Time        `gorm:"autoCreateTime;index" json:"createdAt"`
	ProcessedAt      *time.Time       `json:"processedAt,omitempty"`

	// Relación con detecciones individuales
	Detections []SpaceDetection `gorm:"foreignKey:SnapshotID" json:"detections,omitempty"`
}

// TableName especifica el nombre de la tabla en MySQL
func (ParkingSnapshot) TableName() string {
	return "parking_snapshots"
}

// MarkAsProcessing marca el snapshot como en procesamiento
func (s *ParkingSnapshot) MarkAsProcessing() {
	s.ProcessingStatus = ProcessingStatusProcessing
}

// MarkAsCompleted marca el snapshot como completado exitosamente
func (s *ParkingSnapshot) MarkAsCompleted() {
	s.ProcessingStatus = ProcessingStatusCompleted
	now := time.Now()
	s.ProcessedAt = &now
}

// MarkAsFailed marca el snapshot como fallido con un mensaje de error
func (s *ParkingSnapshot) MarkAsFailed(errorMsg string) {
	s.ProcessingStatus = ProcessingStatusFailed
	s.ErrorMessage = &errorMsg
	now := time.Now()
	s.ProcessedAt = &now
}

// CalculateOccupancy calcula la ocupación en porcentaje
func (s *ParkingSnapshot) CalculateOccupancy() float64 {
	if s.TotalSpaces == 0 {
		return 0
	}
	return (float64(s.OccupiedSpaces) / float64(s.TotalSpaces)) * 100
}
