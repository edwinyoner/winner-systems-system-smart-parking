package middleware

import (
	"fmt"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/sirupsen/logrus"
)

// Logger middleware personalizado para logging de requests
func Logger(logger *logrus.Logger) gin.HandlerFunc {
	return func(c *gin.Context) {
		// Tiempo de inicio
		startTime := time.Now()

		// Información de request
		path := c.Request.URL.Path
		method := c.Request.Method
		clientIP := c.ClientIP()

		// Procesar request
		c.Next()

		// Calcular latencia
		latency := time.Since(startTime)
		statusCode := c.Writer.Status()

		// Determinar nivel de log según status code
		logEntry := logger.WithFields(logrus.Fields{
			"status_code": statusCode,
			"latency_ms":  latency.Milliseconds(),
			"method":      method,
			"path":        path,
			"client_ip":   clientIP,
			"user_agent":  c.Request.UserAgent(),
		})

		// Log según status code
		message := fmt.Sprintf("%s %s - %d", method, path, statusCode)

		switch {
		case statusCode >= 500:
			logEntry.Error(message)
		case statusCode >= 400:
			logEntry.Warn(message)
		case statusCode >= 300:
			logEntry.Info(message)
		default:
			logEntry.Info(message)
		}

		// Si hubo errores en el contexto, logearlos
		if len(c.Errors) > 0 {
			for _, err := range c.Errors {
				logEntry.Error(err.Error())
			}
		}
	}
}

// RequestID middleware para agregar un ID único a cada request
func RequestID() gin.HandlerFunc {
	return func(c *gin.Context) {
		// Generar ID único basado en timestamp + nanotime
		requestID := fmt.Sprintf("%d-%d", time.Now().Unix(), time.Now().Nanosecond())

		// Agregar al contexto y header
		c.Set("RequestID", requestID)
		c.Header("X-Request-ID", requestID)

		c.Next()
	}
}

// ErrorLogger middleware para capturar panics y errores
func ErrorLogger(logger *logrus.Logger) gin.HandlerFunc {
	return func(c *gin.Context) {
		defer func() {
			if err := recover(); err != nil {
				// Log del panic
				logger.WithFields(logrus.Fields{
					"error":  err,
					"path":   c.Request.URL.Path,
					"method": c.Request.Method,
					"ip":     c.ClientIP(),
				}).Error("Panic recuperado")

				// Retornar error 500
				c.JSON(500, gin.H{
					"error":   "Internal Server Error",
					"message": "Ha ocurrido un error inesperado",
				})
				c.Abort()
			}
		}()

		c.Next()
	}
}

// LoggerConfig configuración para el logger middleware
type LoggerConfig struct {
	Logger       *logrus.Logger
	SkipPaths    []string
	LogLatency   bool
	LogUserAgent bool
}

// LoggerWithConfig crea un middleware logger con configuración personalizada
func LoggerWithConfig(config LoggerConfig) gin.HandlerFunc {
	// Crear mapa de paths a ignorar para búsqueda rápida
	skipPaths := make(map[string]bool)
	for _, path := range config.SkipPaths {
		skipPaths[path] = true
	}

	return func(c *gin.Context) {
		path := c.Request.URL.Path

		// Saltar si está en la lista de exclusión
		if skipPaths[path] {
			c.Next()
			return
		}

		startTime := time.Now()
		c.Next()

		latency := time.Since(startTime)
		statusCode := c.Writer.Status()

		fields := logrus.Fields{
			"status_code": statusCode,
			"method":      c.Request.Method,
			"path":        path,
			"client_ip":   c.ClientIP(),
		}

		if config.LogLatency {
			fields["latency_ms"] = latency.Milliseconds()
		}

		if config.LogUserAgent {
			fields["user_agent"] = c.Request.UserAgent()
		}

		logEntry := config.Logger.WithFields(fields)
		message := fmt.Sprintf("%s %s - %d", c.Request.Method, path, statusCode)

		switch {
		case statusCode >= 500:
			logEntry.Error(message)
		case statusCode >= 400:
			logEntry.Warn(message)
		default:
			logEntry.Info(message)
		}
	}
}
