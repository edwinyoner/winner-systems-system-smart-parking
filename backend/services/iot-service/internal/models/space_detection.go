package models

import (
	"time"
)

// SpaceDetection representa la detección de ocupación de un espacio individual
type SpaceDetection struct {
	ID                uint64     `gorm:"primaryKey;autoIncrement" json:"id"`
	SnapshotID        uint64     `gorm:"not null;index" json:"snapshotId"`
	SpaceID           string     `gorm:"type:varchar(20);not null;index" json:"spaceId"`
	Occupied          bool       `gorm:"not null;index" json:"occupied"`
	VehicleType       *string    `gorm:"type:varchar(20)" json:"vehicleType,omitempty"`
	VehicleConfidence *float64   `gorm:"type:decimal(5,3)" json:"vehicleConfidence,omitempty"`
	PlateNumber       *string    `gorm:"type:varchar(20)" json:"plateNumber,omitempty"`
	PlateConfidence   *float64   `gorm:"type:decimal(5,3)" json:"plateConfidence,omitempty"`
	CreatedAt         time.Time  `gorm:"autoCreateTime" json:"createdAt"`
}

// TableName especifica el nombre de la tabla en MySQL
func (SpaceDetection) TableName() string {
	return "space_detections"
}

// HasVehicle verifica si se detectó un vehículo
func (d *SpaceDetection) HasVehicle() bool {
	return d.Occupied && d.VehicleType != nil
}

// HasPlate verifica si se detectó una placa
func (d *SpaceDetection) HasPlate() bool {
	return d.PlateNumber != nil && *d.PlateNumber != ""
}
