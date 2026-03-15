package repository

import (
	"time"

	"github.com/edwinyoner/winner-systems-system-smart-parking/backend/services/iot-service/internal/models"
	"gorm.io/gorm"
)

// DetectionRepository maneja operaciones CRUD de detecciones de espacios
type DetectionRepository struct {
	db *gorm.DB
}

// NewDetectionRepository crea una nueva instancia del repositorio
func NewDetectionRepository(db *gorm.DB) *DetectionRepository {
	return &DetectionRepository{db: db}
}

// Create crea una nueva detección
func (r *DetectionRepository) Create(detection *models.SpaceDetection) error {
	return r.db.Create(detection).Error
}

// BulkCreate crea múltiples detecciones en una transacción
func (r *DetectionRepository) BulkCreate(detections []models.SpaceDetection) error {
	if len(detections) == 0 {
		return nil
	}
	return r.db.Create(&detections).Error
}

// FindByID busca una detección por ID
func (r *DetectionRepository) FindByID(id uint64) (*models.SpaceDetection, error) {
	var detection models.SpaceDetection
	err := r.db.First(&detection, id).Error
	if err != nil {
		return nil, err
	}
	return &detection, nil
}

// FindBySnapshotID busca todas las detecciones de un snapshot
func (r *DetectionRepository) FindBySnapshotID(snapshotID uint64) ([]models.SpaceDetection, error) {
	var detections []models.SpaceDetection
	err := r.db.Where("snapshot_id = ?", snapshotID).
		Order("space_code ASC").
		Find(&detections).Error
	return detections, err
}

// FindBySpaceCode busca todas las detecciones de un espacio específico
func (r *DetectionRepository) FindBySpaceCode(spaceCode string, limit int) ([]models.SpaceDetection, error) {
	var detections []models.SpaceDetection
	query := r.db.Where("space_code = ?", spaceCode).Order("created_at DESC")
	if limit > 0 {
		query = query.Limit(limit)
	}
	err := query.Find(&detections).Error
	return detections, err
}

// FindByParkingID busca todas las detecciones de un parking
func (r *DetectionRepository) FindByParkingID(parkingID uint64, limit int) ([]models.SpaceDetection, error) {
	var detections []models.SpaceDetection
	query := r.db.Where("parking_id = ?", parkingID).Order("created_at DESC")
	if limit > 0 {
		query = query.Limit(limit)
	}
	err := query.Find(&detections).Error
	return detections, err
}

// FindOccupiedBySnapshotID busca espacios ocupados de un snapshot
func (r *DetectionRepository) FindOccupiedBySnapshotID(snapshotID uint64) ([]models.SpaceDetection, error) {
	var detections []models.SpaceDetection
	err := r.db.Where("snapshot_id = ? AND is_occupied = ?", snapshotID, true).
		Order("space_code ASC").
		Find(&detections).Error
	return detections, err
}

// FindAvailableBySnapshotID busca espacios disponibles de un snapshot
func (r *DetectionRepository) FindAvailableBySnapshotID(snapshotID uint64) ([]models.SpaceDetection, error) {
	var detections []models.SpaceDetection
	err := r.db.Where("snapshot_id = ? AND is_occupied = ?", snapshotID, false).
		Order("space_code ASC").
		Find(&detections).Error
	return detections, err
}

// CountBySnapshotID cuenta detecciones de un snapshot
func (r *DetectionRepository) CountBySnapshotID(snapshotID uint64) (int64, error) {
	var count int64
	err := r.db.Model(&models.SpaceDetection{}).
		Where("snapshot_id = ?", snapshotID).
		Count(&count).Error
	return count, err
}

// CountOccupiedBySnapshotID cuenta espacios ocupados de un snapshot
func (r *DetectionRepository) CountOccupiedBySnapshotID(snapshotID uint64) (int64, error) {
	var count int64
	err := r.db.Model(&models.SpaceDetection{}).
		Where("snapshot_id = ? AND is_occupied = ?", snapshotID, true).
		Count(&count).Error
	return count, err
}

// CountAvailableBySnapshotID cuenta espacios disponibles de un snapshot
func (r *DetectionRepository) CountAvailableBySnapshotID(snapshotID uint64) (int64, error) {
	var count int64
	err := r.db.Model(&models.SpaceDetection{}).
		Where("snapshot_id = ? AND is_occupied = ?", snapshotID, false).
		Count(&count).Error
	return count, err
}

// Update actualiza una detección
func (r *DetectionRepository) Update(detection *models.SpaceDetection) error {
	return r.db.Save(detection).Error
}

// Delete elimina una detección
func (r *DetectionRepository) Delete(id uint64) error {
	return r.db.Delete(&models.SpaceDetection{}, id).Error
}

// DeleteBySnapshotID elimina todas las detecciones de un snapshot
func (r *DetectionRepository) DeleteBySnapshotID(snapshotID uint64) (int64, error) {
	result := r.db.Where("snapshot_id = ?", snapshotID).Delete(&models.SpaceDetection{})
	return result.RowsAffected, result.Error
}

// DeleteOlderThan elimina detecciones más antiguas que la fecha especificada
func (r *DetectionRepository) DeleteOlderThan(date time.Time) (int64, error) {
	result := r.db.Where("created_at < ?", date).Delete(&models.SpaceDetection{})
	return result.RowsAffected, result.Error
}

// GetLatestDetectionBySpace obtiene la última detección de un espacio específico
func (r *DetectionRepository) GetLatestDetectionBySpace(parkingID uint64, spaceCode string) (*models.SpaceDetection, error) {
	var detection models.SpaceDetection
	err := r.db.Where("parking_id = ? AND space_code = ?", parkingID, spaceCode).
		Order("created_at DESC").
		First(&detection).Error
	if err != nil {
		return nil, err
	}
	return &detection, nil
}

// GetOccupancyStatsByParkingID obtiene estadísticas de ocupación por parking
func (r *DetectionRepository) GetOccupancyStatsByParkingID(parkingID uint64, since time.Time) (map[string]interface{}, error) {
	var result struct {
		TotalDetections   int64
		OccupiedCount     int64
		AvailableCount    int64
		AverageConfidence float64
	}

	err := r.db.Model(&models.SpaceDetection{}).
		Where("parking_id = ? AND created_at >= ?", parkingID, since).
		Select(`
			COUNT(*) as total_detections,
			SUM(CASE WHEN is_occupied = true THEN 1 ELSE 0 END) as occupied_count,
			SUM(CASE WHEN is_occupied = false THEN 1 ELSE 0 END) as available_count,
			AVG(confidence) as average_confidence
		`).
		Scan(&result).Error

	if err != nil {
		return nil, err
	}

	stats := map[string]interface{}{
		"total_detections":   result.TotalDetections,
		"occupied_count":     result.OccupiedCount,
		"available_count":    result.AvailableCount,
		"average_confidence": result.AverageConfidence,
	}

	return stats, nil
}
