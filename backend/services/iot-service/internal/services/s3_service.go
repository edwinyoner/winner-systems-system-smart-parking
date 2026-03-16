package services

import (
	"bytes"
	"context"
	"fmt"
	"io"
	"time"

	"github.com/minio/minio-go/v7"
	"github.com/minio/minio-go/v7/pkg/credentials"
)

// S3Service servicio para gestionar almacenamiento en MinIO/S3
type S3Service struct {
	client     *minio.Client
	bucketName string
}

// S3Config configuración para S3/MinIO
type S3Config struct {
	Endpoint        string
	AccessKeyID     string
	SecretAccessKey string
	BucketName      string
	UseSSL          bool
}

// NewS3Service crea una nueva instancia del servicio S3
func NewS3Service(config S3Config) (*S3Service, error) {
	// Crear cliente MinIO
	client, err := minio.New(config.Endpoint, &minio.Options{
		Creds:  credentials.NewStaticV4(config.AccessKeyID, config.SecretAccessKey, ""),
		Secure: config.UseSSL,
	})
	if err != nil {
		return nil, fmt.Errorf("error al crear cliente MinIO: %w", err)
	}

	return &S3Service{
		client:     client,
		bucketName: config.BucketName,
	}, nil
}

// EnsureBucket verifica que el bucket exista, si no lo crea
func (s *S3Service) EnsureBucket(ctx context.Context) error {
	exists, err := s.client.BucketExists(ctx, s.bucketName)
	if err != nil {
		return fmt.Errorf("error al verificar existencia del bucket: %w", err)
	}

	if !exists {
		err = s.client.MakeBucket(ctx, s.bucketName, minio.MakeBucketOptions{})
		if err != nil {
			return fmt.Errorf("error al crear bucket: %w", err)
		}
	}

	return nil
}

// UploadImage sube una imagen al bucket
func (s *S3Service) UploadImage(ctx context.Context, objectName string, data []byte, contentType string) (string, error) {
	reader := bytes.NewReader(data)
	size := int64(len(data))

	// Opciones de upload
	opts := minio.PutObjectOptions{
		ContentType: contentType,
	}

	// Subir archivo
	_, err := s.client.PutObject(ctx, s.bucketName, objectName, reader, size, opts)
	if err != nil {
		return "", fmt.Errorf("error al subir imagen: %w", err)
	}

	// Generar URL pública (válida por 7 días)
	url, err := s.GetPresignedURL(ctx, objectName, 7*24*time.Hour)
	if err != nil {
		return "", fmt.Errorf("error al generar URL: %w", err)
	}

	return url, nil
}

// UploadImageFromReader sube una imagen desde un io.Reader
func (s *S3Service) UploadImageFromReader(ctx context.Context, objectName string, reader io.Reader, size int64, contentType string) (string, error) {
	opts := minio.PutObjectOptions{
		ContentType: contentType,
	}

	_, err := s.client.PutObject(ctx, s.bucketName, objectName, reader, size, opts)
	if err != nil {
		return "", fmt.Errorf("error al subir imagen: %w", err)
	}

	url, err := s.GetPresignedURL(ctx, objectName, 7*24*time.Hour)
	if err != nil {
		return "", fmt.Errorf("error al generar URL: %w", err)
	}

	return url, nil
}

// GetPresignedURL genera una URL prefirmada para acceder a un objeto
func (s *S3Service) GetPresignedURL(ctx context.Context, objectName string, expiry time.Duration) (string, error) {
	// Pasar nil si no hay parámetros adicionales de query
	url, err := s.client.PresignedGetObject(ctx, s.bucketName, objectName, expiry, nil)
	if err != nil {
		return "", fmt.Errorf("error al generar URL prefirmada: %w", err)
	}

	return url.String(), nil
}

// DeleteObject elimina un objeto del bucket
func (s *S3Service) DeleteObject(ctx context.Context, objectName string) error {
	err := s.client.RemoveObject(ctx, s.bucketName, objectName, minio.RemoveObjectOptions{})
	if err != nil {
		return fmt.Errorf("error al eliminar objeto: %w", err)
	}
	return nil
}

// DeleteObjects elimina múltiples objetos del bucket
func (s *S3Service) DeleteObjects(ctx context.Context, objectNames []string) error {
	objectsCh := make(chan minio.ObjectInfo, len(objectNames))

	// Enviar nombres de objetos al canal
	go func() {
		defer close(objectsCh)
		for _, name := range objectNames {
			objectsCh <- minio.ObjectInfo{Key: name}
		}
	}()

	// Eliminar objetos
	errorCh := s.client.RemoveObjects(ctx, s.bucketName, objectsCh, minio.RemoveObjectsOptions{})

	// Verificar errores
	for err := range errorCh {
		if err.Err != nil {
			return fmt.Errorf("error al eliminar objeto %s: %w", err.ObjectName, err.Err)
		}
	}

	return nil
}

// ObjectExists verifica si un objeto existe en el bucket
func (s *S3Service) ObjectExists(ctx context.Context, objectName string) (bool, error) {
	_, err := s.client.StatObject(ctx, s.bucketName, objectName, minio.StatObjectOptions{})
	if err != nil {
		errResponse := minio.ToErrorResponse(err)
		if errResponse.Code == "NoSuchKey" {
			return false, nil
		}
		return false, fmt.Errorf("error al verificar existencia del objeto: %w", err)
	}
	return true, nil
}

// GenerateObjectName genera un nombre único para un objeto
// Formato: captures/2026/03/13/device-123_1678901234.jpg
func GenerateObjectName(prefix, deviceID, extension string) string {
	now := time.Now()
	timestamp := now.Unix()
	date := now.Format("2006/01/02")

	return fmt.Sprintf("%s/%s/%s_%d.%s", prefix, date, deviceID, timestamp, extension)
}

// GenerateSnapshotObjectName genera nombre para snapshots aéreos
func GenerateSnapshotObjectName(parkingID uint64, deviceID string) string {
	now := time.Now()
	timestamp := now.Unix()
	date := now.Format("2006/01/02")

	return fmt.Sprintf("snapshots/parking-%d/%s/%s_%d.jpg", parkingID, date, deviceID, timestamp)
}
