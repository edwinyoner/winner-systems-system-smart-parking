package com.winnersystems.smartparking.parking.domain.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad de dominio que representa una TRANSACCIÓN de estacionamiento.
 *
 * Transaction es la entidad MÁS IMPORTANTE del sistema. Registra el ciclo completo
 * desde que un vehículo entra hasta que sale y paga.
 *
 * Ciclo de vida:
 * 1. ENTRADA: Se crea con status ACTIVE y paymentStatus PENDING
 * 2. SALIDA: Se actualiza con exitTime, calcula monto, status COMPLETED
 * 3. PAGO: Se crea Payment asociado, paymentStatus cambia a PAID
 * 4. COMPROBANTE: Se envía automáticamente por WhatsApp/Email
 *
 * Esta entidad conecta TODO el sistema:
 * - Vehículo (qué auto)
 * - Cliente (quién lo dejó/retiró)
 * - Zona y Espacio (dónde se estacionó)
 * - Tarifa (cuánto cuesta)
 * - Operadores (quién registró entrada/salida)
 * - Pago (cómo y cuándo pagó)
 *
 * Seguridad anti-robo:
 * - Guarda documento de entrada y salida
 * - Sistema valida que coincidan
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public class Transaction {

   // ========================= CAMPOS DE IDENTIDAD =========================

   private Long id;

   // ========================= RELACIONES (FKs) =========================

   private Long vehicleId;                                // FK a Vehicle (NOT NULL)
   private Long customerId;                               // FK a Customer (NOT NULL)
   private Long parkingId;                                // FK a Parking (NOT NULL)
   private Long zoneId;                                   // FK a ParkingZone (NOT NULL)
   private Long rateId;                                   // FK a Rate (NOT NULL)
   // Tarifa aplicada al momento de ENTRADA

   // ========================= SEGURIDAD - DOCUMENTOS DUPLICADOS =========================

   private Long entryDocumentTypeId;                      // FK a DocumentType (NOT NULL)
   private String entryDocumentNumber;                    // Documento usado en ENTRADA (NOT NULL)

   private Long exitDocumentTypeId;                       // FK a DocumentType (nullable)
   private String exitDocumentNumber;                     // Documento usado en SALIDA (nullable)
   // Sistema valida: entry == exit (anti-robo)

   // ========================= TIEMPOS =========================

   private LocalDateTime entryTime;                       // Hora de entrada (NOT NULL)
   private LocalDateTime exitTime;                        // Hora de salida (nullable - NULL si aún dentro)
   private Integer durationMinutes;                       // Duración en minutos (calculado)

   // ========================= OPERADORES Y MÉTODOS =========================

   private Long entryOperatorId;                          // FK a User/auth (NOT NULL)
   private Long exitOperatorId;                           // FK a User/auth (nullable)

   private String entryMethod;                            // MANUAL, CAMERA_AI, SENSOR
   private String exitMethod;                             // MANUAL, CAMERA_AI, SENSOR

   // ========================= EVIDENCIA (FOTOS DE CÁMARAS) =========================

   private String entryPhotoUrl;                          // URL foto entrada (nullable)
   private String exitPhotoUrl;                           // URL foto salida (nullable)

   private Double entryPlateConfidence;                   // Confianza IA placa entrada (0.0-1.0)
   private Double exitPlateConfidence;                    // Confianza IA placa salida (0.0-1.0)

   // ========================= MONTOS =========================

   private BigDecimal calculatedAmount;                   // Monto calculado (tarifa × duración)
   private BigDecimal discountAmount;                     // Descuentos aplicados (default 0.00)
   private BigDecimal totalAmount;                        // Monto final a pagar
   private String currency;                               // Moneda (default "PEN")

   // ========================= ESTADOS =========================

   private String status;                                 // ACTIVE, COMPLETED, CANCELLED
   private String paymentStatus;                          // PENDING, PAID, OVERDUE

   // ========================= COMPROBANTE DIGITAL =========================

   private Boolean receiptSent;                           // ¿Se envió comprobante?
   private LocalDateTime receiptSentAt;                   // Cuándo se envió
   private String receiptWhatsAppStatus;                  // SENT, DELIVERED, READ, FAILED
   private String receiptEmailStatus;                     // SENT, DELIVERED, OPENED, FAILED

   // ========================= NOTAS Y OBSERVACIONES =========================

   private String notes;                                  // Observaciones generales
   private String cancellationReason;                     // Razón si fue cancelada

   // ========================= AUDITORÍA =========================

   private LocalDateTime createdAt;
   private Long createdBy;
   private LocalDateTime updatedAt;
   private Long updatedBy;

   // ========================= CONSTANTES - ESTADOS DE TRANSACCIÓN =========================

   public static final String STATUS_ACTIVE = "ACTIVE";
   public static final String STATUS_COMPLETED = "COMPLETED";
   public static final String STATUS_CANCELLED = "CANCELLED";

   // ========================= CONSTANTES - ESTADOS DE PAGO =========================

   public static final String PAYMENT_STATUS_PENDING = "PENDING";
   public static final String PAYMENT_STATUS_PAID = "PAID";
   public static final String PAYMENT_STATUS_OVERDUE = "OVERDUE";

   // ========================= CONSTANTES - MÉTODOS DE REGISTRO =========================

   public static final String METHOD_MANUAL = "MANUAL";
   public static final String METHOD_CAMERA_AI = "CAMERA_AI";
   public static final String METHOD_SENSOR = "SENSOR";

   // ========================= CONSTRUCTORES =========================

   /**
    * Constructor vacío - Inicializa con valores por defecto.
    */
   public Transaction() {
      this.status = STATUS_ACTIVE;
      this.paymentStatus = PAYMENT_STATUS_PENDING;
      this.currency = "PEN";
      this.discountAmount = BigDecimal.ZERO;
      this.receiptSent = false;
      this.createdAt = LocalDateTime.now();
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Constructor para ENTRADA de vehículo.
    *
    * @param vehicleId ID del vehículo
    * @param customerId ID del cliente
    * @param parkingId ID del espacio
    * @param zoneId ID de la zona
    * @param rateId ID de la tarifa
    * @param entryDocumentTypeId tipo de documento entrada
    * @param entryDocumentNumber número de documento entrada
    */
   public Transaction(Long vehicleId, Long customerId, Long parkingId, Long zoneId,
                      Long rateId, Long entryDocumentTypeId, String entryDocumentNumber) {
      this();
      this.vehicleId = vehicleId;
      this.customerId = customerId;
      this.parkingId = parkingId;
      this.zoneId = zoneId;
      this.rateId = rateId;
      this.entryDocumentTypeId = entryDocumentTypeId;
      this.entryDocumentNumber = entryDocumentNumber;
      this.entryTime = LocalDateTime.now();
      this.entryMethod = METHOD_MANUAL;
   }

   // ========================= MÉTODOS DE NEGOCIO - ENTRADA =========================

   /**
    * Registra la entrada del vehículo con operador.
    *
    * @param operatorId ID del operador que registra
    */
   public void recordEntry(Long operatorId) {
      this.entryOperatorId = operatorId;
      this.entryTime = LocalDateTime.now();
      this.status = STATUS_ACTIVE;
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Registra evidencia fotográfica de entrada.
    *
    * @param photoUrl URL de la foto
    * @param plateConfidence confianza de la IA en la placa
    */
   public void recordEntryPhoto(String photoUrl, Double plateConfidence) {
      this.entryPhotoUrl = photoUrl;
      this.entryPlateConfidence = plateConfidence;
      this.updatedAt = LocalDateTime.now();
   }

   // ========================= MÉTODOS DE NEGOCIO - SALIDA =========================

   /**
    * Registra la salida del vehículo.
    *
    * @param exitDocumentTypeId tipo de documento salida
    * @param exitDocumentNumber número de documento salida
    * @param operatorId operador que registra salida
    */
   public void recordExit(Long exitDocumentTypeId, String exitDocumentNumber, Long operatorId) {
      this.exitDocumentTypeId = exitDocumentTypeId;
      this.exitDocumentNumber = exitDocumentNumber;
      this.exitOperatorId = operatorId;
      this.exitTime = LocalDateTime.now();
      this.status = STATUS_COMPLETED;
      this.calculateDuration();
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Calcula la duración del estacionamiento.
    */
   public void calculateDuration() {
      if (entryTime != null && exitTime != null) {
         Duration duration = Duration.between(entryTime, exitTime);
         this.durationMinutes = (int) duration.toMinutes();
      }
   }

   /**
    * Registra evidencia fotográfica de salida.
    *
    * @param photoUrl URL de la foto
    * @param plateConfidence confianza de la IA en la placa
    */
   public void recordExitPhoto(String photoUrl, Double plateConfidence) {
      this.exitPhotoUrl = photoUrl;
      this.exitPlateConfidence = plateConfidence;
      this.updatedAt = LocalDateTime.now();
   }

   // ========================= MÉTODOS DE NEGOCIO - SEGURIDAD =========================

   /**
    * Verifica que el documento de salida coincida con el de entrada.
    * Seguridad anti-robo.
    *
    * @return true si los documentos coinciden
    */
   public boolean verifyDocumentMatch() {
      if (entryDocumentNumber == null || exitDocumentNumber == null) {
         return false;
      }
      return entryDocumentNumber.equals(exitDocumentNumber) &&
            Objects.equals(entryDocumentTypeId, exitDocumentTypeId);
   }

   // ========================= MÉTODOS DE NEGOCIO - CÁLCULO DE MONTOS =========================

   /**
    * Calcula el monto a pagar basado en tarifa y duración.
    *
    * @param hourlyRate tarifa por hora
    */
   public void calculateAmount(BigDecimal hourlyRate) {
      if (durationMinutes == null || hourlyRate == null) {
         return;
      }

      double hours = durationMinutes / 60.0;
      this.calculatedAmount = hourlyRate.multiply(BigDecimal.valueOf(hours))
            .setScale(2, BigDecimal.ROUND_HALF_UP);
      this.calculateTotal();
   }

   /**
    * Aplica un descuento.
    *
    * @param discount monto del descuento
    */
   public void applyDiscount(BigDecimal discount) {
      this.discountAmount = discount;
      this.calculateTotal();
   }

   /**
    * Calcula el monto total final.
    */
   public void calculateTotal() {
      if (calculatedAmount == null) {
         this.totalAmount = BigDecimal.ZERO;
         return;
      }

      BigDecimal discount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
      this.totalAmount = calculatedAmount.subtract(discount);

      if (this.totalAmount.compareTo(BigDecimal.ZERO) < 0) {
         this.totalAmount = BigDecimal.ZERO;
      }
   }

   // ========================= MÉTODOS DE NEGOCIO - PAGO =========================

   /**
    * Marca la transacción como pagada.
    */
   public void markAsPaid() {
      this.paymentStatus = PAYMENT_STATUS_PAID;
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Marca la transacción como vencida (sin pago).
    */
   public void markAsOverdue() {
      this.paymentStatus = PAYMENT_STATUS_OVERDUE;
      this.updatedAt = LocalDateTime.now();
   }

   // ========================= MÉTODOS DE NEGOCIO - COMPROBANTE =========================

   /**
    * Registra el envío del comprobante digital.
    */
   public void markReceiptAsSent() {
      this.receiptSent = true;
      this.receiptSentAt = LocalDateTime.now();
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Actualiza el estado de envío por WhatsApp.
    *
    * @param status estado del envío
    */
   public void updateWhatsAppStatus(String status) {
      this.receiptWhatsAppStatus = status;
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Actualiza el estado de envío por Email.
    *
    * @param status estado del envío
    */
   public void updateEmailStatus(String status) {
      this.receiptEmailStatus = status;
      this.updatedAt = LocalDateTime.now();
   }

   // ========================= MÉTODOS DE NEGOCIO - CANCELACIÓN =========================

   /**
    * Cancela la transacción.
    *
    * @param reason razón de la cancelación
    * @param userId usuario que cancela
    */
   public void cancel(String reason, Long userId) {
      this.status = STATUS_CANCELLED;
      this.cancellationReason = reason;
      this.updatedBy = userId;
      this.updatedAt = LocalDateTime.now();
   }

   // ========================= MÉTODOS DE CONSULTA - ESTADOS =========================

   /**
    * Verifica si el vehículo está actualmente dentro del parqueo.
    *
    * @return true si status = ACTIVE
    */
   public boolean isActive() {
      return STATUS_ACTIVE.equals(status);
   }

   /**
    * Verifica si la transacción está completada.
    *
    * @return true si status = COMPLETED
    */
   public boolean isCompleted() {
      return STATUS_COMPLETED.equals(status);
   }

   /**
    * Verifica si la transacción fue cancelada.
    *
    * @return true si status = CANCELLED
    */
   public boolean isCancelled() {
      return STATUS_CANCELLED.equals(status);
   }

   /**
    * Verifica si está pendiente de pago.
    *
    * @return true si paymentStatus = PENDING
    */
   public boolean isPending() {
      return PAYMENT_STATUS_PENDING.equals(paymentStatus);
   }

   /**
    * Verifica si ya fue pagada.
    *
    * @return true si paymentStatus = PAID
    */
   public boolean isPaid() {
      return PAYMENT_STATUS_PAID.equals(paymentStatus);
   }

   /**
    * Verifica si está vencida (sin pago).
    *
    * @return true si paymentStatus = OVERDUE
    */
   public boolean isOverdue() {
      return PAYMENT_STATUS_OVERDUE.equals(paymentStatus);
   }

   /**
    * Verifica si el vehículo aún está dentro.
    *
    * @return true si exitTime es null
    */
   public boolean isVehicleInside() {
      return exitTime == null;
   }

   /**
    * Verifica si el comprobante fue enviado.
    *
    * @return true si receiptSent = true
    */
   public boolean isReceiptSent() {
      return Boolean.TRUE.equals(receiptSent);
   }

   // ========================= MÉTODOS DE CONSULTA - INFORMACIÓN =========================

   /**
    * Obtiene la duración en formato legible.
    *
    * @return duración formateada (ej: "3h 30min")
    */
   public String getFormattedDuration() {
      if (durationMinutes == null) {
         return "0min";
      }

      int hours = durationMinutes / 60;
      int minutes = durationMinutes % 60;

      if (hours > 0 && minutes > 0) {
         return hours + "h " + minutes + "min";
      } else if (hours > 0) {
         return hours + "h";
      } else {
         return minutes + "min";
      }
   }

   /**
    * Obtiene una descripción completa de la transacción.
    *
    * @return descripción formateada
    */
   public String getDescription() {
      return "Transaction #" + id + " [" + status + "/" + paymentStatus + "]";
   }

   // ========================= MÉTODOS DE VALIDACIÓN =========================

   /**
    * Verifica si la transacción tiene todos los datos necesarios para entrada.
    *
    * @return true si tiene datos completos de entrada
    */
   public boolean hasCompleteEntryData() {
      return vehicleId != null && customerId != null && parkingId != null &&
            zoneId != null && rateId != null && entryTime != null &&
            entryDocumentTypeId != null && entryDocumentNumber != null;
   }

   /**
    * Verifica si la transacción tiene todos los datos necesarios para salida.
    *
    * @return true si tiene datos completos de salida
    */
   public boolean hasCompleteExitData() {
      return exitTime != null && exitDocumentTypeId != null &&
            exitDocumentNumber != null && durationMinutes != null;
   }

   /**
    * Verifica si tiene montos calculados.
    *
    * @return true si tiene calculatedAmount y totalAmount
    */
   public boolean hasCalculatedAmounts() {
      return calculatedAmount != null && totalAmount != null;
   }

   // ========================= GETTERS Y SETTERS =========================

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public Long getVehicleId() {
      return vehicleId;
   }

   public void setVehicleId(Long vehicleId) {
      this.vehicleId = vehicleId;
   }

   public Long getCustomerId() {
      return customerId;
   }

   public void setCustomerId(Long customerId) {
      this.customerId = customerId;
   }

   public Long getParkingId() {
      return parkingId;
   }

   public void setParkingId(Long parkingId) {
      this.parkingId = parkingId;
   }

   public Long getZoneId() {
      return zoneId;
   }

   public void setZoneId(Long zoneId) {
      this.zoneId = zoneId;
   }

   public Long getRateId() {
      return rateId;
   }

   public void setRateId(Long rateId) {
      this.rateId = rateId;
   }

   public Long getEntryDocumentTypeId() {
      return entryDocumentTypeId;
   }

   public void setEntryDocumentTypeId(Long entryDocumentTypeId) {
      this.entryDocumentTypeId = entryDocumentTypeId;
   }

   public String getEntryDocumentNumber() {
      return entryDocumentNumber;
   }

   public void setEntryDocumentNumber(String entryDocumentNumber) {
      this.entryDocumentNumber = entryDocumentNumber;
   }

   public Long getExitDocumentTypeId() {
      return exitDocumentTypeId;
   }

   public void setExitDocumentTypeId(Long exitDocumentTypeId) {
      this.exitDocumentTypeId = exitDocumentTypeId;
   }

   public String getExitDocumentNumber() {
      return exitDocumentNumber;
   }

   public void setExitDocumentNumber(String exitDocumentNumber) {
      this.exitDocumentNumber = exitDocumentNumber;
   }

   public LocalDateTime getEntryTime() {
      return entryTime;
   }

   public void setEntryTime(LocalDateTime entryTime) {
      this.entryTime = entryTime;
   }

   public LocalDateTime getExitTime() {
      return exitTime;
   }

   public void setExitTime(LocalDateTime exitTime) {
      this.exitTime = exitTime;
   }

   public Integer getDurationMinutes() {
      return durationMinutes;
   }

   public void setDurationMinutes(Integer durationMinutes) {
      this.durationMinutes = durationMinutes;
   }

   public Long getEntryOperatorId() {
      return entryOperatorId;
   }

   public void setEntryOperatorId(Long entryOperatorId) {
      this.entryOperatorId = entryOperatorId;
   }

   public Long getExitOperatorId() {
      return exitOperatorId;
   }

   public void setExitOperatorId(Long exitOperatorId) {
      this.exitOperatorId = exitOperatorId;
   }

   public String getEntryMethod() {
      return entryMethod;
   }

   public void setEntryMethod(String entryMethod) {
      this.entryMethod = entryMethod;
   }

   public String getExitMethod() {
      return exitMethod;
   }

   public void setExitMethod(String exitMethod) {
      this.exitMethod = exitMethod;
   }

   public String getEntryPhotoUrl() {
      return entryPhotoUrl;
   }

   public void setEntryPhotoUrl(String entryPhotoUrl) {
      this.entryPhotoUrl = entryPhotoUrl;
   }

   public String getExitPhotoUrl() {
      return exitPhotoUrl;
   }

   public void setExitPhotoUrl(String exitPhotoUrl) {
      this.exitPhotoUrl = exitPhotoUrl;
   }

   public Double getEntryPlateConfidence() {
      return entryPlateConfidence;
   }

   public void setEntryPlateConfidence(Double entryPlateConfidence) {
      this.entryPlateConfidence = entryPlateConfidence;
   }

   public Double getExitPlateConfidence() {
      return exitPlateConfidence;
   }

   public void setExitPlateConfidence(Double exitPlateConfidence) {
      this.exitPlateConfidence = exitPlateConfidence;
   }

   public BigDecimal getCalculatedAmount() {
      return calculatedAmount;
   }

   public void setCalculatedAmount(BigDecimal calculatedAmount) {
      this.calculatedAmount = calculatedAmount;
   }

   public BigDecimal getDiscountAmount() {
      return discountAmount;
   }

   public void setDiscountAmount(BigDecimal discountAmount) {
      this.discountAmount = discountAmount;
   }

   public BigDecimal getTotalAmount() {
      return totalAmount;
   }

   public void setTotalAmount(BigDecimal totalAmount) {
      this.totalAmount = totalAmount;
   }

   public String getCurrency() {
      return currency;
   }

   public void setCurrency(String currency) {
      this.currency = currency;
   }

   public String getStatus() {
      return status;
   }

   public void setStatus(String status) {
      this.status = status;
   }

   public String getPaymentStatus() {
      return paymentStatus;
   }

   public void setPaymentStatus(String paymentStatus) {
      this.paymentStatus = paymentStatus;
   }

   public Boolean getReceiptSent() {
      return receiptSent;
   }

   public void setReceiptSent(Boolean receiptSent) {
      this.receiptSent = receiptSent;
   }

   public LocalDateTime getReceiptSentAt() {
      return receiptSentAt;
   }

   public void setReceiptSentAt(LocalDateTime receiptSentAt) {
      this.receiptSentAt = receiptSentAt;
   }

   public String getReceiptWhatsAppStatus() {
      return receiptWhatsAppStatus;
   }

   public void setReceiptWhatsAppStatus(String receiptWhatsAppStatus) {
      this.receiptWhatsAppStatus = receiptWhatsAppStatus;
   }

   public String getReceiptEmailStatus() {
      return receiptEmailStatus;
   }

   public void setReceiptEmailStatus(String receiptEmailStatus) {
      this.receiptEmailStatus = receiptEmailStatus;
   }

   public String getNotes() {
      return notes;
   }

   public void setNotes(String notes) {
      this.notes = notes;
   }

   public String getCancellationReason() {
      return cancellationReason;
   }

   public void setCancellationReason(String cancellationReason) {
      this.cancellationReason = cancellationReason;
   }

   public LocalDateTime getCreatedAt() {
      return createdAt;
   }

   public void setCreatedAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
   }

   public Long getCreatedBy() {
      return createdBy;
   }

   public void setCreatedBy(Long createdBy) {
      this.createdBy = createdBy;
   }

   public LocalDateTime getUpdatedAt() {
      return updatedAt;
   }

   public void setUpdatedAt(LocalDateTime updatedAt) {
      this.updatedAt = updatedAt;
   }

   public Long getUpdatedBy() {
      return updatedBy;
   }

   public void setUpdatedBy(Long updatedBy) {
      this.updatedBy = updatedBy;
   }

   // ========================= EQUALS, HASHCODE Y TOSTRING =========================

   /**
    * Dos transacciones son iguales si tienen el mismo ID.
    */
   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Transaction that = (Transaction) o;
      return Objects.equals(id, that.id);
   }

   /**
    * HashCode basado en ID únicamente.
    */
   @Override
   public int hashCode() {
      return Objects.hash(id);
   }

   /**
    * ToString para logging y debugging.
    */
   @Override
   public String toString() {
      return "Transaction{" +
            "id=" + id +
            ", vehicleId=" + vehicleId +
            ", customerId=" + customerId +
            ", status='" + status + '\'' +
            ", paymentStatus='" + paymentStatus + '\'' +
            ", entryTime=" + entryTime +
            ", exitTime=" + exitTime +
            ", duration='" + getFormattedDuration() + '\'' +
            ", totalAmount=" + totalAmount +
            ", receiptSent=" + receiptSent +
            '}';
   }
}