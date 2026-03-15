package repository

import (
	"time"

	"github.com/edwinyoner/winner-systems-system-smart-parking/backend/services/iot-service/internal/models"
	"gorm.io/gorm"
)

// SnapshotRepository maneja operaciones CRUD de snapshots de parking
type SnapshotRepository struct {
	db *gorm.DB
}

// NewSnapshotRepository crea una nueva instancia del repositorio
func NewSnapshotRepository(db *gorm.DB) *SnapshotRepository {
	return &SnapshotRepository{db: db}
}

// Create crea un nuevo snapshot
func (r *SnapshotRepository) Create(snapshot *models.ParkingSnapshot) error {
	return r.db.Create(snapshot).Error
}

// FindByID busca un snapshot por ID
func (r *SnapshotRepository) FindByID(id uint64) (*models.ParkingSnapshot, error) {
	var snapshot models.ParkingSnapshot
	err := r.db.First(&snapshot, id).Error
	if err != nil {
		return nil, err
	}
	return &snapshot, nil
}

// FindBySnapshotID busca un snapshot por su snapshotId único
func (r *SnapshotRepository) FindBySnapshotID(snapshotID string) (*models.ParkingSnapshot, error) {
	var snapshot models.ParkingSnapshot
	err := r.db.Where("snapshot_id = ?", snapshotID).First(&snapshot).Error
	if err != nil {
		return nil, err
	}
	return &snapshot, nil
}

// FindBySnapshotIDWithDetections busca un snapshot con sus detecciones precargadas
func (r *SnapshotRepository) FindBySnapshotIDWithDetections(snapshotID string) (*models.ParkingSnapshot, error) {
	var snapshot models.ParkingSnapshot
	err := r.db.Preload("Detections").Where("snapshot_id = ?", snapshotID).First(&snapshot).Error
	if err != nil {
		return nil, err
	}
	return &snapshot, nil
}

// FindByDeviceID busca todos los snapshots de un dispositivo
func (r *SnapshotRepository) FindByDeviceID(deviceID string, limit int) ([]models.ParkingSnapshot, error) {
	var snapshots []models.ParkingSnapshot
	query := r.db.Where("device_id = ?", deviceID).Order("created_at DESC")
	if limit > 0 {
		query = query.Limit(limit)
	}
	err := query.Find(&snapshots).Error
	return snapshots, err
}

// FindByParkingID busca todos los snapshots de un parking
func (r *SnapshotRepository) FindByParkingID(parkingID uint64, limit int) ([]models.ParkingSnapshot, error) {
	var snapshots []models.ParkingSnapshot
	query := r.db.Where("parking_id = ?", parkingID).Order("created_at DESC")
	if limit > 0 {
		query = query.Limit(limit)
	}
	err := query.Find(&snapshots).Error
	return snapshots, err
}

// FindLatestByParkingID busca el snapshot más reciente de un parking
func (r *SnapshotRepository) FindLatestByParkingID(parkingID uint64) (*models.ParkingSnapshot, error) {
	var snapshot models.ParkingSnapshot
	err := r.db.Where("parking_id = ?", parkingID).
		Order("created_at DESC").
		First(&snapshot).Error
	if err != nil {
		return nil, err
	}
	return &snapshot, nil
}

// FindLatestByParkingIDWithDetections busca el snapshot más reciente con detecciones
func (r *SnapshotRepository) FindLatestByParkingIDWithDetections(parkingID uint64) (*models.ParkingSnapshot, error) {
	var snapshot models.ParkingSnapshot
	err := r.db.Preload("Detections").
		Where("parking_id = ?", parkingID).
		Order("created_at DESC").
		First(&snapshot).Error
	if err != nil {
		return nil, err
	}
	return &snapshot, nil
}

// FindByStatus busca snapshots por estado de procesamiento
func (r *SnapshotRepository) FindByStatus(status models.ProcessingStatus, limit int) ([]models.ParkingSnapshot, error) {
	var snapshots []models.ParkingSnapshot
	query := r.db.Where("processing_status = ?", status).Order("created_at ASC")
	if limit > 0 {
		query = query.Limit(limit)
	}
	err := query.Find(&snapshots).Error
	return snapshots, err
}

// FindPendingSnapshots busca snapshots pendientes de procesar
func (r *SnapshotRepository) FindPendingSnapshots(limit int) ([]models.ParkingSnapshot, error) {
	return r.FindByStatus(models.ProcessingStatusPending, limit)
}

// Update actualiza un snapshot
func (r *SnapshotRepository) Update(snapshot *models.ParkingSnapshot) error {
	return r.db.Save(snapshot).Error
}

// UpdateStatus actualiza solo el estado de procesamiento
func (r *SnapshotRepository) UpdateStatus(snapshotID string, status models.ProcessingStatus) error {
	return r.db.Model(&models.ParkingSnapshot{}).
		Where("snapshot_id = ?", snapshotID).
		Update("processing_status", status).Error
}

// MarkAsProcessing marca un snapshot como en procesamiento
func (r *SnapshotRepository) MarkAsProcessing(snapshotID string) error {
	return r.UpdateStatus(snapshotID, models.ProcessingStatusProcessing)
}

// MarkAsCompleted marca un snapshot como completado
func (r *SnapshotRepository) MarkAsCompleted(snapshotID string, totalSpaces, occupiedSpaces int) error {
	now := time.Now()
	return r.db.Model(&models.ParkingSnapshot{}).
		Where("snapshot_id = ?", snapshotID).
		Updates(map[string]interface{}{
			"processing_status": models.ProcessingStatusCompleted,
			"total_spaces":      totalSpaces,
			"occupied_spaces":   occupiedSpaces,
			"processed_at":      now,
		}).Error
}

// MarkAsFailed marca un snapshot como fallido
func (r *SnapshotRepository) MarkAsFailed(snapshotID string, errorMsg string) error {
	now := time.Now()
	return r.db.Model(&models.ParkingSnapshot{}).
		Where("snapshot_id = ?", snapshotID).
		Updates(map[string]interface{}{
			"processing_status": models.ProcessingStatusFailed,
			"error_message":     errorMsg,
			"processed_at":      now,
		}).Error
}

// Delete elimina un snapshot
func (r *SnapshotRepository) Delete(id uint64) error {
	return r.db.Delete(&models.ParkingSnapshot{}, id).Error
}

// DeleteOlderThan elimina snapshots más antiguos que la fecha especificada
func (r *SnapshotRepository) DeleteOlderThan(date time.Time) (int64, error) {
	result := r.db.Where("created_at < ?", date).Delete(&models.ParkingSnapshot{})
	return result.RowsAffected, result.Error
}

// CountByStatus cuenta snapshots por estado
func (r *SnapshotRepository) CountByStatus(status models.ProcessingStatus) (int64, error) {
	var count int64
	err := r.db.Model(&models.ParkingSnapshot{}).Where("processing_status = ?", status).Count(&count).Error
	return count, err
}

// CountByParkingID cuenta snapshots de un parking específico
func (r *SnapshotRepository) CountByParkingID(parkingID uint64) (int64, error) {
	var count int64
	err := r.db.Model(&models.ParkingSnapshot{}).Where("parking_id = ?", parkingID).Count(&count).Error
	return count, err
}
