package repository

import (
	"time"

	"github.com/edwinyoner/winner-systems-system-smart-parking/backend/services/iot-service/internal/models"
	"gorm.io/gorm"
)

// DeviceRepository maneja operaciones CRUD de dispositivos
type DeviceRepository struct {
	db *gorm.DB
}

// NewDeviceRepository crea una nueva instancia del repositorio
func NewDeviceRepository(db *gorm.DB) *DeviceRepository {
	return &DeviceRepository{db: db}
}

// Create crea un nuevo dispositivo
func (r *DeviceRepository) Create(device *models.Device) error {
	return r.db.Create(device).Error
}

// FindByID busca un dispositivo por ID
func (r *DeviceRepository) FindByID(id uint64) (*models.Device, error) {
	var device models.Device
	err := r.db.First(&device, id).Error
	if err != nil {
		return nil, err
	}
	return &device, nil
}

// FindByDeviceID busca un dispositivo por su deviceId único
func (r *DeviceRepository) FindByDeviceID(deviceID string) (*models.Device, error) {
	var device models.Device
	err := r.db.Where("device_id = ?", deviceID).First(&device).Error
	if err != nil {
		return nil, err
	}
	return &device, nil
}

// FindByParkingID busca todos los dispositivos de un parking
func (r *DeviceRepository) FindByParkingID(parkingID uint64) ([]models.Device, error) {
	var devices []models.Device
	err := r.db.Where("parking_id = ?", parkingID).Find(&devices).Error
	return devices, err
}

// FindByType busca dispositivos por tipo
func (r *DeviceRepository) FindByType(deviceType models.DeviceType) ([]models.Device, error) {
	var devices []models.Device
	err := r.db.Where("device_type = ?", deviceType).Find(&devices).Error
	return devices, err
}

// FindActiveDevices busca todos los dispositivos activos
func (r *DeviceRepository) FindActiveDevices() ([]models.Device, error) {
	var devices []models.Device
	err := r.db.Where("status = ?", models.DeviceStatusActive).Find(&devices).Error
	return devices, err
}

// Update actualiza un dispositivo
func (r *DeviceRepository) Update(device *models.Device) error {
	return r.db.Save(device).Error
}

// UpdateHeartbeat actualiza el timestamp del último heartbeat
func (r *DeviceRepository) UpdateHeartbeat(deviceID string) error {
	now := time.Now()
	return r.db.Model(&models.Device{}).
		Where("device_id = ?", deviceID).
		Update("last_heartbeat", now).Error
}

// UpdateStatus actualiza el estado de un dispositivo
func (r *DeviceRepository) UpdateStatus(deviceID string, status models.DeviceStatus) error {
	return r.db.Model(&models.Device{}).
		Where("device_id = ?", deviceID).
		Update("status", status).Error
}

// Delete elimina un dispositivo (soft delete si está configurado)
func (r *DeviceRepository) Delete(id uint64) error {
	return r.db.Delete(&models.Device{}, id).Error
}

// FindAll obtiene todos los dispositivos con paginación
func (r *DeviceRepository) FindAll(page, pageSize int) ([]models.Device, int64, error) {
	var devices []models.Device
	var total int64

	// Contar total
	if err := r.db.Model(&models.Device{}).Count(&total).Error; err != nil {
		return nil, 0, err
	}

	// Obtener página
	offset := (page - 1) * pageSize
	err := r.db.Offset(offset).Limit(pageSize).Find(&devices).Error
	if err != nil {
		return nil, 0, err
	}

	return devices, total, nil
}

// Exists verifica si existe un dispositivo con el deviceID dado
func (r *DeviceRepository) Exists(deviceID string) (bool, error) {
	var count int64
	err := r.db.Model(&models.Device{}).Where("device_id = ?", deviceID).Count(&count).Error
	return count > 0, err
}
