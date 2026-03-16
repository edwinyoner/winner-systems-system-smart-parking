package repository

import (
	"time"

	"github.com/edwinyoner/winner-systems-system-smart-parking/backend/services/iot-service/internal/models"
	"gorm.io/gorm"
)

// DisplayLogRepository maneja operaciones CRUD de logs de displays LED
type DisplayLogRepository struct {
	db *gorm.DB
}

// NewDisplayLogRepository crea una nueva instancia del repositorio
func NewDisplayLogRepository(db *gorm.DB) *DisplayLogRepository {
	return &DisplayLogRepository{db: db}
}

// Create crea un nuevo log de display
func (r *DisplayLogRepository) Create(log *models.DisplayLog) error {
	return r.db.Create(log).Error
}

// FindByID busca un log por ID
func (r *DisplayLogRepository) FindByID(id uint64) (*models.DisplayLog, error) {
	var log models.DisplayLog
	err := r.db.First(&log, id).Error
	if err != nil {
		return nil, err
	}
	return &log, nil
}

// FindByDeviceID busca logs de un dispositivo específico
func (r *DisplayLogRepository) FindByDeviceID(deviceID string, limit int) ([]models.DisplayLog, error) {
	var logs []models.DisplayLog
	query := r.db.Where("device_id = ?", deviceID).Order("created_at DESC")
	if limit > 0 {
		query = query.Limit(limit)
	}
	err := query.Find(&logs).Error
	return logs, err
}

// FindByParkingID busca logs de un parking específico
func (r *DisplayLogRepository) FindByParkingID(parkingID uint64, limit int) ([]models.DisplayLog, error) {
	var logs []models.DisplayLog
	query := r.db.Where("parking_id = ?", parkingID).Order("created_at DESC")
	if limit > 0 {
		query = query.Limit(limit)
	}
	err := query.Find(&logs).Error
	return logs, err
}

// FindByDateRange busca logs en un rango de fechas
func (r *DisplayLogRepository) FindByDateRange(startDate, endDate time.Time, limit int) ([]models.DisplayLog, error) {
	var logs []models.DisplayLog
	query := r.db.Where("created_at BETWEEN ? AND ?", startDate, endDate).Order("created_at DESC")
	if limit > 0 {
		query = query.Limit(limit)
	}
	err := query.Find(&logs).Error
	return logs, err
}

// FindRecentByDevice busca logs recientes de un dispositivo (últimas 24h por defecto)
func (r *DisplayLogRepository) FindRecentByDevice(deviceID string, hours int) ([]models.DisplayLog, error) {
	if hours <= 0 {
		hours = 24
	}
	since := time.Now().Add(-time.Duration(hours) * time.Hour)
	var logs []models.DisplayLog
	err := r.db.Where("device_id = ? AND created_at >= ?", deviceID, since).
		Order("created_at DESC").
		Find(&logs).Error
	return logs, err
}

// CountByDeviceID cuenta logs de un dispositivo
func (r *DisplayLogRepository) CountByDeviceID(deviceID string) (int64, error) {
	var count int64
	err := r.db.Model(&models.DisplayLog{}).Where("device_id = ?", deviceID).Count(&count).Error
	return count, err
}

// CountByParkingID cuenta logs de un parking
func (r *DisplayLogRepository) CountByParkingID(parkingID uint64) (int64, error) {
	var count int64
	err := r.db.Model(&models.DisplayLog{}).Where("parking_id = ?", parkingID).Count(&count).Error
	return count, err
}

// CountByDateRange cuenta logs en un rango de fechas
func (r *DisplayLogRepository) CountByDateRange(startDate, endDate time.Time) (int64, error) {
	var count int64
	err := r.db.Model(&models.DisplayLog{}).
		Where("created_at BETWEEN ? AND ?", startDate, endDate).
		Count(&count).Error
	return count, err
}

// Delete elimina un log
func (r *DisplayLogRepository) Delete(id uint64) error {
	return r.db.Delete(&models.DisplayLog{}, id).Error
}

// DeleteOlderThan elimina logs más antiguos que la fecha especificada
func (r *DisplayLogRepository) DeleteOlderThan(date time.Time) (int64, error) {
	result := r.db.Where("created_at < ?", date).Delete(&models.DisplayLog{})
	return result.RowsAffected, result.Error
}

// DeleteByDeviceID elimina todos los logs de un dispositivo
func (r *DisplayLogRepository) DeleteByDeviceID(deviceID string) (int64, error) {
	result := r.db.Where("device_id = ?", deviceID).Delete(&models.DisplayLog{})
	return result.RowsAffected, result.Error
}

// GetAnalyticsByDevice obtiene estadísticas de consultas por dispositivo
func (r *DisplayLogRepository) GetAnalyticsByDevice(deviceID string, since time.Time) (map[string]interface{}, error) {
	var result struct {
		TotalQueries      int64
		CacheHits         int64
		CacheMisses       int64
		AverageResponseMs float64
		TotalAvailable    int64
		TotalOccupied     int64
	}

	err := r.db.Model(&models.DisplayLog{}).
		Where("device_id = ? AND created_at >= ?", deviceID, since).
		Select(`
			COUNT(*) as total_queries,
			SUM(CASE WHEN cache_hit = true THEN 1 ELSE 0 END) as cache_hits,
			SUM(CASE WHEN cache_hit = false THEN 1 ELSE 0 END) as cache_misses,
			AVG(response_time_ms) as average_response_ms,
			SUM(available_spaces) as total_available,
			SUM(occupied_spaces) as total_occupied
		`).
		Scan(&result).Error

	if err != nil {
		return nil, err
	}

	var cacheHitRate float64
	if result.TotalQueries > 0 {
		cacheHitRate = float64(result.CacheHits) / float64(result.TotalQueries) * 100
	}

	analytics := map[string]interface{}{
		"total_queries":       result.TotalQueries,
		"cache_hits":          result.CacheHits,
		"cache_misses":        result.CacheMisses,
		"cache_hit_rate":      cacheHitRate,
		"average_response_ms": result.AverageResponseMs,
		"total_available":     result.TotalAvailable,
		"total_occupied":      result.TotalOccupied,
	}

	return analytics, nil
}

// GetAnalyticsByParkingID obtiene estadísticas de consultas por parking
func (r *DisplayLogRepository) GetAnalyticsByParkingID(parkingID uint64, since time.Time) (map[string]interface{}, error) {
	var result struct {
		TotalQueries      int64
		CacheHits         int64
		CacheMisses       int64
		AverageResponseMs float64
		UniqueDevices     int64
	}

	err := r.db.Model(&models.DisplayLog{}).
		Where("parking_id = ? AND created_at >= ?", parkingID, since).
		Select(`
			COUNT(*) as total_queries,
			SUM(CASE WHEN cache_hit = true THEN 1 ELSE 0 END) as cache_hits,
			SUM(CASE WHEN cache_hit = false THEN 1 ELSE 0 END) as cache_misses,
			AVG(response_time_ms) as average_response_ms,
			COUNT(DISTINCT device_id) as unique_devices
		`).
		Scan(&result).Error

	if err != nil {
		return nil, err
	}

	var cacheHitRate float64
	if result.TotalQueries > 0 {
		cacheHitRate = float64(result.CacheHits) / float64(result.TotalQueries) * 100
	}

	analytics := map[string]interface{}{
		"total_queries":       result.TotalQueries,
		"cache_hits":          result.CacheHits,
		"cache_misses":        result.CacheMisses,
		"cache_hit_rate":      cacheHitRate,
		"average_response_ms": result.AverageResponseMs,
		"unique_devices":      result.UniqueDevices,
	}

	return analytics, nil
}

// GetHourlyStats obtiene estadísticas agrupadas por hora
func (r *DisplayLogRepository) GetHourlyStats(parkingID uint64, date time.Time) ([]map[string]interface{}, error) {
	startOfDay := time.Date(date.Year(), date.Month(), date.Day(), 0, 0, 0, 0, date.Location())
	endOfDay := startOfDay.Add(24 * time.Hour)

	var results []struct {
		Hour         int
		QueryCount   int64
		CacheHits    int64
		AvgAvailable float64
		AvgOccupied  float64
	}

	err := r.db.Model(&models.DisplayLog{}).
		Where("parking_id = ? AND created_at BETWEEN ? AND ?", parkingID, startOfDay, endOfDay).
		Select(`
			HOUR(created_at) as hour,
			COUNT(*) as query_count,
			SUM(CASE WHEN cache_hit = true THEN 1 ELSE 0 END) as cache_hits,
			AVG(available_spaces) as avg_available,
			AVG(occupied_spaces) as avg_occupied
		`).
		Group("HOUR(created_at)").
		Order("hour ASC").
		Scan(&results).Error

	if err != nil {
		return nil, err
	}

	stats := make([]map[string]interface{}, len(results))
	for i, r := range results {
		stats[i] = map[string]interface{}{
			"hour":          r.Hour,
			"query_count":   r.QueryCount,
			"cache_hits":    r.CacheHits,
			"avg_available": r.AvgAvailable,
			"avg_occupied":  r.AvgOccupied,
		}
	}

	return stats, nil
}
