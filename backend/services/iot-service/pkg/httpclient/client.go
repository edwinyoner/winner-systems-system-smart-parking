package httpclient

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"time"
)

// Client es un cliente HTTP reutilizable con retry logic y timeouts
type Client struct {
	httpClient *http.Client
	baseURL    string
	timeout    time.Duration
	maxRetries int
}

// Config contiene la configuración del cliente HTTP
type Config struct {
	BaseURL    string
	Timeout    time.Duration
	MaxRetries int
}

// NewClient crea una nueva instancia del cliente HTTP
func NewClient(config Config) *Client {
	if config.Timeout == 0 {
		config.Timeout = 30 * time.Second
	}
	if config.MaxRetries == 0 {
		config.MaxRetries = 3
	}

	return &Client{
		httpClient: &http.Client{
			Timeout: config.Timeout,
		},
		baseURL:    config.BaseURL,
		timeout:    config.Timeout,
		maxRetries: config.MaxRetries,
	}
}

// Response representa una respuesta HTTP genérica
type Response struct {
	StatusCode int
	Body       []byte
	Headers    http.Header
}

// Get realiza una petición GET
func (c *Client) Get(ctx context.Context, path string, headers map[string]string) (*Response, error) {
	return c.doRequest(ctx, http.MethodGet, path, nil, headers)
}

// Post realiza una petición POST
func (c *Client) Post(ctx context.Context, path string, body interface{}, headers map[string]string) (*Response, error) {
	return c.doRequest(ctx, http.MethodPost, path, body, headers)
}

// Put realiza una petición PUT
func (c *Client) Put(ctx context.Context, path string, body interface{}, headers map[string]string) (*Response, error) {
	return c.doRequest(ctx, http.MethodPut, path, body, headers)
}

// Delete realiza una petición DELETE
func (c *Client) Delete(ctx context.Context, path string, headers map[string]string) (*Response, error) {
	return c.doRequest(ctx, http.MethodDelete, path, nil, headers)
}

// doRequest realiza la petición HTTP con retry logic
func (c *Client) doRequest(ctx context.Context, method, path string, body interface{}, headers map[string]string) (*Response, error) {
	url := c.baseURL + path

	var reqBody io.Reader
	if body != nil {
		jsonData, err := json.Marshal(body)
		if err != nil {
			return nil, fmt.Errorf("error al serializar body: %w", err)
		}
		reqBody = bytes.NewBuffer(jsonData)
	}

	var lastErr error
	for attempt := 0; attempt <= c.maxRetries; attempt++ {
		if attempt > 0 {
			// Espera exponencial entre reintentos
			backoff := time.Duration(attempt) * time.Second
			select {
			case <-ctx.Done():
				return nil, ctx.Err()
			case <-time.After(backoff):
			}
		}

		// Crear request
		req, err := http.NewRequestWithContext(ctx, method, url, reqBody)
		if err != nil {
			return nil, fmt.Errorf("error al crear request: %w", err)
		}

		// Agregar headers
		req.Header.Set("Content-Type", "application/json")
		for key, value := range headers {
			req.Header.Set(key, value)
		}

		// Ejecutar request
		resp, err := c.httpClient.Do(req)
		if err != nil {
			lastErr = fmt.Errorf("error en intento %d: %w", attempt+1, err)
			continue
		}

		// Leer body
		responseBody, err := io.ReadAll(resp.Body)
		resp.Body.Close()
		if err != nil {
			lastErr = fmt.Errorf("error al leer response body en intento %d: %w", attempt+1, err)
			continue
		}

		// Verificar si es necesario reintentar basado en status code
		if c.shouldRetry(resp.StatusCode) && attempt < c.maxRetries {
			lastErr = fmt.Errorf("status code %d en intento %d", resp.StatusCode, attempt+1)
			continue
		}

		// Retornar respuesta exitosa
		return &Response{
			StatusCode: resp.StatusCode,
			Body:       responseBody,
			Headers:    resp.Header,
		}, nil
	}

	return nil, fmt.Errorf("máximo de reintentos alcanzado: %w", lastErr)
}

// shouldRetry determina si un status code amerita reintento
func (c *Client) shouldRetry(statusCode int) bool {
	// Reintentar en errores de servidor (5xx) y algunos errores de cliente
	return statusCode >= 500 || statusCode == 429 || statusCode == 408
}

// DecodeJSON decodifica el body de la respuesta a un struct
func (r *Response) DecodeJSON(v interface{}) error {
	if err := json.Unmarshal(r.Body, v); err != nil {
		return fmt.Errorf("error al decodificar JSON: %w", err)
	}
	return nil
}

// IsSuccess verifica si el status code indica éxito (2xx)
func (r *Response) IsSuccess() bool {
	return r.StatusCode >= 200 && r.StatusCode < 300
}

// GetError retorna un error descriptivo si el status no es exitoso
func (r *Response) GetError() error {
	if r.IsSuccess() {
		return nil
	}

	// Intentar parsear error del body
	var errorResp struct {
		Error   string `json:"error"`
		Message string `json:"message"`
		Detail  string `json:"detail"`
	}

	if err := json.Unmarshal(r.Body, &errorResp); err == nil {
		if errorResp.Error != "" {
			return fmt.Errorf("HTTP %d: %s", r.StatusCode, errorResp.Error)
		}
		if errorResp.Message != "" {
			return fmt.Errorf("HTTP %d: %s", r.StatusCode, errorResp.Message)
		}
		if errorResp.Detail != "" {
			return fmt.Errorf("HTTP %d: %s", r.StatusCode, errorResp.Detail)
		}
	}

	return fmt.Errorf("HTTP %d: %s", r.StatusCode, string(r.Body))
}
