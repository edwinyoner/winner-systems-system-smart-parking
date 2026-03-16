package repository

import (
	"time"

	"github.com/edwinyoner/winner-systems-system-smart-parking/backend/services/iot-service/internal/models"
	"gorm.io/gorm"
)

// CaptureRepository maneja operaciones CRUD de capturas de cámara
type CaptureRepository struct {
	db *gorm.DB
}

// NewCaptureRepository crea una nueva instancia del repositorio
func NewCaptureRepository(db *gorm.DB) *CaptureRepository {
	return &CaptureRepository{db: db}
}

// Create crea una nueva captura
func (r *CaptureRepository) Create(capture *models.CameraCapture) error {
	return r.db.Create(capture).Error
}

// FindByID busca una captura por ID
func (r *CaptureRepository) FindByID(id uint64) (*models.CameraCapture, error) {
	var capture models.CameraCapture
	err := r.db.First(&capture, id).Error
	if err != nil {
		return nil, err
	}
	return &capture, nil
}

// FindByCaptureID busca una captura por su captureId único
func (r *CaptureRepository) FindByCaptureID(captureID string) (*models.CameraCapture, error) {
	var capture models.CameraCapture
	err := r.db.Where("capture_id = ?", captureID).First(&capture).Error
	if err != nil {
		return nil, err
	}
	return &capture, nil
}

// FindByDeviceID busca todas las capturas de un dispositivo
func (r *CaptureRepository) FindByDeviceID(deviceID string, limit int) ([]models.CameraCapture, error) {
	var captures []models.CameraCapture
	query := r.db.Where("device_id = ?", deviceID).Order("created_at DESC")
	if limit > 0 {
		query = query.Limit(limit)
	}
	err := query.Find(&captures).Error
	return captures, err
}

// FindByParkingID busca todas las capturas de un parking
func (r *CaptureRepository) FindByParkingID(parkingID uint64, limit int) ([]models.CameraCapture, error) {
	var captures []models.CameraCapture
	query := r.db.Where("parking_id = ?", parkingID).Order("created_at DESC")
	if limit > 0 {
		query = query.Limit(limit)
	}
	err := query.Find(&captures).Error
	return captures, err
}

// FindByStatus busca capturas por estado de procesamiento
func (r *CaptureRepository) FindByStatus(status models.ProcessingStatus, limit int) ([]models.CameraCapture, error) {
	var captures []models.CameraCapture
	query := r.db.Where("processing_status = ?", status).Order("created_at ASC")
	if limit > 0 {
		query = query.Limit(limit)
	}
	err := query.Find(&captures).Error
	return captures, err
}

// FindPendingCaptures busca capturas pendientes de procesar
func (r *CaptureRepository) FindPendingCaptures(limit int) ([]models.CameraCapture, error) {
	return r.FindByStatus(models.ProcessingStatusPending, limit)
}

// Update actualiza una captura
func (r *CaptureRepository) Update(capture *models.CameraCapture) error {
	return r.db.Save(capture).Error
}

// UpdateStatus actualiza solo el estado de procesamiento
func (r *CaptureRepository) UpdateStatus(captureID string, status models.ProcessingStatus) error {
	return r.db.Model(&models.CameraCapture{}).
		Where("capture_id = ?", captureID).
		Update("processing_status", status).Error
}

// MarkAsProcessing marca una captura como en procesamiento
func (r *CaptureRepository) MarkAsProcessing(captureID string) error {
	return r.UpdateStatus(captureID, models.ProcessingStatusProcessing)
}

// MarkAsCompleted marca una captura como completada
func (r *CaptureRepository) MarkAsCompleted(captureID string, plateNumber string, confidence float64, transactionID uint64) error {
	now := time.Now()
	return r.db.Model(&models.CameraCapture{}).
		Where("capture_id = ?", captureID).
		Updates(map[string]interface{}{
			"processing_status": models.ProcessingStatusCompleted,
			"plate_number":      plateNumber,
			"plate_confidence":  confidence,
			"transaction_id":    transactionID,
			"processed_at":      now,
		}).Error
}

// MarkAsFailed marca una captura como fallida
func (r *CaptureRepository) MarkAsFailed(captureID string, errorMsg string) error {
	now := time.Now()
	return r.db.Model(&models.CameraCapture{}).
		Where("capture_id = ?", captureID).
		Updates(map[string]interface{}{
			"processing_status": models.ProcessingStatusFailed,
			"error_message":     errorMsg,
			"processed_at":      now,
		}).Error
}

// Delete elimina una captura
func (r *CaptureRepository) Delete(id uint64) error {
	return r.db.Delete(&models.CameraCapture{}, id).Error
}

// DeleteOlderThan elimina capturas más antiguas que la fecha especificada
func (r *CaptureRepository) DeleteOlderThan(date time.Time) (int64, error) {
	result := r.db.Where("created_at < ?", date).Delete(&models.CameraCapture{})
	return result.RowsAffected, result.Error
}

// CountByStatus cuenta capturas por estado
func (r *CaptureRepository) CountByStatus(status models.ProcessingStatus) (int64, error) {
	var count int64
	err := r.db.Model(&models.CameraCapture{}).Where("processing_status = ?", status).Count(&count).Error
	return count, err
}
