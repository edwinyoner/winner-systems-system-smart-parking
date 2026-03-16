package models

import (
	"time"
)

// DisplayLog representa un log de consulta de disponibilidad de un display LED
type DisplayLog struct {
	ID              uint64    `gorm:"primaryKey;autoIncrement" json:"id"`
	DeviceID        string    `gorm:"type:varchar(50);not null;index" json:"deviceId"`
	ParkingID       uint64    `gorm:"not null;index" json:"parkingId"`
	TotalSpaces     int       `gorm:"not null" json:"totalSpaces"`
	OccupiedSpaces  int       `gorm:"not null" json:"occupiedSpaces"`
	AvailableSpaces int       `gorm:"not null" json:"availableSpaces"`
	ResponseTimeMs  *int      `json:"responseTimeMs,omitempty"`
	FromCache       bool      `gorm:"default:false" json:"fromCache"`
	CreatedAt       time.Time `gorm:"autoCreateTime;index" json:"createdAt"`
}

// TableName especifica el nombre de la tabla en MySQL
func (DisplayLog) TableName() string {
	return "display_logs"
}

// WasFromCache verifica si la respuesta vino de cache
func (l *DisplayLog) WasFromCache() bool {
	return l.FromCache
}

// CalculateOccupancyPercentage calcula el porcentaje de ocupación
func (l *DisplayLog) CalculateOccupancyPercentage() float64 {
	if l.TotalSpaces == 0 {
		return 0
	}
	return (float64(l.OccupiedSpaces) / float64(l.TotalSpaces)) * 100
}
