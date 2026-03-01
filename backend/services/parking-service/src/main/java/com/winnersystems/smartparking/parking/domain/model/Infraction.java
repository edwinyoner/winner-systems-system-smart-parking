package com.winnersystems.smartparking.parking.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad de dominio que representa una INFRACCIÓN o INCIDENTE.
 *
 * Registra violaciones a las normas de estacionamiento y problemas operativos.
 * Cada infracción está asociada a una transacción específica (cuando aplica).
 *
 * Tipos de infracciones:
 * - OVERSTAY: Excedió tiempo máximo sin pagar
 * - WRONG_SPACE: Vehículo en espacio incorrecto (ej: discapacitados sin permiso)
 * - DOUBLE_PARKING: Ocupa múltiples espacios
 * - ABANDONED: Vehículo abandonado (más de 24-48h)
 * - DAMAGE: Daños a propiedad municipal
 * - UNPAID_EXIT: Intentó salir sin pagar
 * - NO_REGISTRATION: Vehículo sin registro de entrada
 * - OTHER: Otras violaciones
 *
 * Estados de infracción:
 * - PENDING: Registrada, pendiente de resolución
 * - IN_REVIEW: En revisión por autoridad
 * - RESOLVED: Resuelta (pagada o desestimada)
 * - ESCALATED: Escalada a autoridad superior
 *
 * Las infracciones pueden generar multas (fine) que deben ser pagadas.
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public class Infraction {

   // ========================= CAMPOS DE IDENTIDAD =========================

   private Long id;
   private String infractionCode;                         // Código único (ej: INF-2026-001234)

   // ========================= RELACIONES (FKs) =========================

   private Long parkingId;                                // FK a Parking (NOT NULL)
   private Long zoneId;                                   // FK a Zone (NOT NULL)
   private Long spaceId;                                  // FK a Space (nullable)

   private Long transactionId;                            // FK a Transaction (nullable)
   // Nullable porque puede haber infracciones
   // sin transacción (vehículo nunca registró entrada)

   private Long vehicleId;                                // FK a Vehicle (NOT NULL)
   private Long customerId;                               // FK a Customer (nullable)

   // ========================= CAMPOS DE INFRACCIÓN =========================

   private String infractionType;                         // Tipo de infracción
   private String severity;                               // MINOR, MODERATE, SEVERE, CRITICAL

   private LocalDateTime detectedAt;                      // Cuándo se detectó (NOT NULL)
   private Long detectedBy;                               // FK User - Quien la registró
   private String detectionMethod;                        // MANUAL, CAMERA_AI, SENSOR, SYSTEM

   private String description;                            // Descripción detallada
   private String evidence;                               // URLs de fotos/videos (JSON o separado por comas)

   // ========================= CAMPOS DE MULTA =========================

   private BigDecimal fineAmount;                         // Monto de la multa (nullable)
   private String currency;                               // Moneda (default "PEN")
   private LocalDateTime fineDueDate;                     // Fecha límite para pagar multa

   // ========================= CAMPOS DE RESOLUCIÓN =========================

   private String status;                                 // PENDING, IN_REVIEW, RESOLVED, ESCALATED

   private LocalDateTime resolvedAt;                      // Cuándo se resolvió (nullable)
   private Long resolvedBy;                               // FK User - Quien resolvió
   private String resolution;                             // Descripción de la resolución
   private String resolutionType;                         // PAID, DISMISSED, ESCALATED

   // ========================= CAMPOS DE PAGO DE MULTA =========================

   private Boolean finePaid;                              // ¿Se pagó la multa?
   private LocalDateTime finePaidAt;                      // Cuándo se pagó
   private BigDecimal finePaidAmount;                     // Monto pagado
   private String finePaymentReference;                   // Número de referencia del pago

   // ========================= CAMPOS DE NOTIFICACIÓN =========================

   private Boolean notificationSent;                      // ¿Se notificó al cliente?
   private LocalDateTime notificationSentAt;              // Cuándo se notificó
   private String notificationMethod;                     // EMAIL, SMS, WHATSAPP, MAIL

   // ========================= OBSERVACIONES =========================

   private String notes;                                  // Notas adicionales

   // ========================= AUDITORÍA =========================

   private LocalDateTime createdAt;
   private Long createdBy;
   private LocalDateTime updatedAt;
   private Long updatedBy;
   private LocalDateTime deletedAt;                       // Soft delete
   private Long deletedBy;

   // ========================= CONSTANTES - TIPOS DE INFRACCIÓN =========================

   public static final String TYPE_OVERSTAY = "OVERSTAY";
   public static final String TYPE_WRONG_SPACE = "WRONG_SPACE";
   public static final String TYPE_DOUBLE_PARKING = "DOUBLE_PARKING";
   public static final String TYPE_ABANDONED = "ABANDONED";
   public static final String TYPE_DAMAGE = "DAMAGE";
   public static final String TYPE_UNPAID_EXIT = "UNPAID_EXIT";
   public static final String TYPE_NO_REGISTRATION = "NO_REGISTRATION";
   public static final String TYPE_OTHER = "OTHER";

   // ========================= CONSTANTES - SEVERIDAD =========================

   public static final String SEVERITY_MINOR = "MINOR";
   public static final String SEVERITY_MODERATE = "MODERATE";
   public static final String SEVERITY_SEVERE = "SEVERE";
   public static final String SEVERITY_CRITICAL = "CRITICAL";

   // ========================= CONSTANTES - ESTADOS =========================

   public static final String STATUS_PENDING = "PENDING";
   public static final String STATUS_IN_REVIEW = "IN_REVIEW";
   public static final String STATUS_RESOLVED = "RESOLVED";
   public static final String STATUS_ESCALATED = "ESCALATED";

   // ========================= CONSTANTES - TIPOS DE RESOLUCIÓN =========================

   public static final String RESOLUTION_PAID = "PAID";
   public static final String RESOLUTION_DISMISSED = "DISMISSED";
   public static final String RESOLUTION_ESCALATED = "ESCALATED";

   // ========================= CONSTANTES - MÉTODOS DE DETECCIÓN =========================

   public static final String METHOD_MANUAL = "MANUAL";
   public static final String METHOD_CAMERA_AI = "CAMERA_AI";
   public static final String METHOD_SENSOR = "SENSOR";
   public static final String METHOD_SYSTEM = "SYSTEM";

   // ========================= CONSTRUCTORES =========================

   /**
    * Constructor vacío - Inicializa con valores por defecto.
    */
   public Infraction() {
      this.status = STATUS_PENDING;
      this.currency = "PEN";
      this.finePaid = false;
      this.notificationSent = false;
      this.detectedAt = LocalDateTime.now();
      this.createdAt = LocalDateTime.now();
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Constructor básico para infracción.
    *
    * @param parkingId ID del parking
    * @param vehicleId ID del vehículo infractor
    * @param zoneId ID de la zona
    * @param infractionType tipo de infracción
    * @param detectedBy quien detectó
    */
   public Infraction(Long parkingId, Long vehicleId, Long zoneId,
                     String infractionType, Long detectedBy) {
      this();
      this.parkingId = parkingId;
      this.vehicleId = vehicleId;
      this.zoneId = zoneId;
      this.infractionType = infractionType;
      this.detectedBy = detectedBy;
   }

   // ========================= MÉTODOS DE NEGOCIO - REGISTRO =========================

   /**
    * Genera código único de infracción.
    * Formato: INF-YYYY-NNNNNN
    *
    * NOTA: Este método debe llamarse DESPUÉS de que el ID sea asignado por la base de datos.
    * Típicamente se llama desde el servicio después de persistir.
    */
   public void generateInfractionCode() {
      if (this.id == null) {
         throw new IllegalStateException("Cannot generate infraction code without ID");
      }
      int year = this.detectedAt != null ? this.detectedAt.getYear() : LocalDateTime.now().getYear();
      this.infractionCode = String.format("INF-%d-%06d", year, this.id);
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Registra evidencia fotográfica/video.
    *
    * @param evidenceUrls URLs separadas por comas
    */
   public void attachEvidence(String evidenceUrls) {
      this.evidence = evidenceUrls;
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Asigna multa a la infracción.
    *
    * @param amount monto de la multa
    * @param dueDate fecha límite para pagar
    */
   public void assignFine(BigDecimal amount, LocalDateTime dueDate) {
      this.fineAmount = amount;
      this.fineDueDate = dueDate;
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Asigna multa con fecha de vencimiento automática (7 días).
    *
    * @param amount monto de la multa
    */
   public void assignFine(BigDecimal amount) {
      this.fineAmount = amount;
      this.fineDueDate = LocalDateTime.now().plusDays(7);
      this.updatedAt = LocalDateTime.now();
   }

   // ========================= MÉTODOS DE NEGOCIO - RESOLUCIÓN =========================

   /**
    * Marca la infracción como en revisión.
    *
    * @param reviewerId quien revisa
    */
   public void markInReview(Long reviewerId) {
      this.status = STATUS_IN_REVIEW;
      this.resolvedBy = reviewerId;
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Resuelve la infracción como pagada.
    *
    * @param resolvedById quien resuelve
    * @param resolution descripción de resolución
    */
   public void resolveAsPaid(Long resolvedById, String resolution) {
      this.status = STATUS_RESOLVED;
      this.resolvedAt = LocalDateTime.now();
      this.resolvedBy = resolvedById;
      this.resolution = resolution;
      this.resolutionType = RESOLUTION_PAID;
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Desestima la infracción.
    *
    * @param resolvedById quien desestima
    * @param reason razón
    */
   public void dismiss(Long resolvedById, String reason) {
      this.status = STATUS_RESOLVED;
      this.resolvedAt = LocalDateTime.now();
      this.resolvedBy = resolvedById;
      this.resolution = reason;
      this.resolutionType = RESOLUTION_DISMISSED;
      this.fineAmount = BigDecimal.ZERO;
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Escala la infracción a autoridad superior.
    *
    * @param escalatedById quien escala
    * @param reason razón
    */
   public void escalate(Long escalatedById, String reason) {
      this.status = STATUS_ESCALATED;
      this.resolvedBy = escalatedById;
      this.resolution = reason;
      this.resolutionType = RESOLUTION_ESCALATED;
      this.updatedAt = LocalDateTime.now();
   }

   // ========================= MÉTODOS DE NEGOCIO - PAGO DE MULTA =========================

   /**
    * Registra el pago de la multa.
    *
    * @param amount monto pagado
    * @param reference número de referencia
    */
   public void recordFinePayment(BigDecimal amount, String reference) {
      this.finePaid = true;
      this.finePaidAt = LocalDateTime.now();
      this.finePaidAmount = amount;
      this.finePaymentReference = reference;
      this.updatedAt = LocalDateTime.now();
   }

   // ========================= MÉTODOS DE NEGOCIO - NOTIFICACIÓN =========================

   /**
    * Marca la notificación como enviada.
    *
    * @param method método usado (EMAIL, SMS, etc.)
    */
   public void markNotificationSent(String method) {
      this.notificationSent = true;
      this.notificationSentAt = LocalDateTime.now();
      this.notificationMethod = method;
      this.updatedAt = LocalDateTime.now();
   }

   // ========================= MÉTODOS DE NEGOCIO - SOFT DELETE =========================

   /**
    * Marca la infracción como eliminada (soft delete).
    *
    * @param deletedByUserId ID del usuario que elimina
    */
   public void markAsDeleted(Long deletedByUserId) {
      this.deletedAt = LocalDateTime.now();
      this.deletedBy = deletedByUserId;
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Restaura una infracción eliminada.
    */
   public void restore() {
      this.deletedAt = null;
      this.deletedBy = null;
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Verifica si está eliminada.
    *
    * @return true si deletedAt no es null
    */
   public boolean isDeleted() {
      return deletedAt != null;
   }

   // ========================= MÉTODOS DE CONSULTA - ESTADOS =========================

   /**
    * Verifica si está pendiente.
    *
    * @return true si status = PENDING
    */
   public boolean isPending() {
      return STATUS_PENDING.equals(status);
   }

   /**
    * Verifica si está en revisión.
    *
    * @return true si status = IN_REVIEW
    */
   public boolean isInReview() {
      return STATUS_IN_REVIEW.equals(status);
   }

   /**
    * Verifica si está resuelta.
    *
    * @return true si status = RESOLVED
    */
   public boolean isResolved() {
      return STATUS_RESOLVED.equals(status);
   }

   /**
    * Verifica si fue escalada.
    *
    * @return true si status = ESCALATED
    */
   public boolean isEscalated() {
      return STATUS_ESCALATED.equals(status);
   }

   /**
    * Verifica si tiene multa asignada.
    *
    * @return true si fineAmount > 0
    */
   public boolean hasFine() {
      return fineAmount != null && fineAmount.compareTo(BigDecimal.ZERO) > 0;
   }

   /**
    * Verifica si la multa fue pagada.
    *
    * @return true si finePaid = true
    */
   public boolean isFinePaid() {
      return Boolean.TRUE.equals(finePaid);
   }

   /**
    * Verifica si la multa está vencida.
    *
    * @return true si fineDueDate < now
    */
   public boolean isFineOverdue() {
      return fineDueDate != null &&
            !isFinePaid() &&
            LocalDateTime.now().isAfter(fineDueDate);
   }

   /**
    * Verifica si tiene evidencia adjunta.
    *
    * @return true si evidence no es null
    */
   public boolean hasEvidence() {
      return evidence != null && !evidence.isEmpty();
   }

   /**
    * Verifica si se envió notificación.
    *
    * @return true si notificationSent = true
    */
   public boolean isNotificationSent() {
      return Boolean.TRUE.equals(notificationSent);
   }

   // ========================= MÉTODOS DE INFORMACIÓN =========================

   /**
    * Obtiene descripción completa de la infracción.
    *
    * @return descripción formateada
    */
   public String getFullDescription() {
      if (infractionCode == null) {
         return infractionType + " [" + severity + "]";
      }
      return infractionCode + " - " + infractionType + " [" + severity + "]";
   }

   /**
    * Calcula días restantes para pagar la multa.
    *
    * @return días restantes (negativo si ya venció)
    */
   public long getDaysUntilDue() {
      if (fineDueDate == null) {
         return 0;
      }
      return java.time.Duration.between(LocalDateTime.now(), fineDueDate).toDays();
   }

   // ========================= MÉTODOS DE ACTUALIZACIÓN =========================

   /**
    * Actualiza el usuario que modificó.
    *
    * @param userId ID del usuario
    */
   public void updateModifiedBy(Long userId) {
      this.updatedBy = userId;
      this.updatedAt = LocalDateTime.now();
   }

   // ========================= GETTERS Y SETTERS =========================

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public String getInfractionCode() {
      return infractionCode;
   }

   public void setInfractionCode(String infractionCode) {
      this.infractionCode = infractionCode;
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

   public Long getSpaceId() {
      return spaceId;
   }

   public void setSpaceId(Long spaceId) {
      this.spaceId = spaceId;
   }

   public Long getTransactionId() {
      return transactionId;
   }

   public void setTransactionId(Long transactionId) {
      this.transactionId = transactionId;
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

   public String getInfractionType() {
      return infractionType;
   }

   public void setInfractionType(String infractionType) {
      this.infractionType = infractionType;
   }

   public String getSeverity() {
      return severity;
   }

   public void setSeverity(String severity) {
      this.severity = severity;
   }

   public LocalDateTime getDetectedAt() {
      return detectedAt;
   }

   public void setDetectedAt(LocalDateTime detectedAt) {
      this.detectedAt = detectedAt;
   }

   public Long getDetectedBy() {
      return detectedBy;
   }

   public void setDetectedBy(Long detectedBy) {
      this.detectedBy = detectedBy;
   }

   public String getDetectionMethod() {
      return detectionMethod;
   }

   public void setDetectionMethod(String detectionMethod) {
      this.detectionMethod = detectionMethod;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public String getEvidence() {
      return evidence;
   }

   public void setEvidence(String evidence) {
      this.evidence = evidence;
   }

   public BigDecimal getFineAmount() {
      return fineAmount;
   }

   public void setFineAmount(BigDecimal fineAmount) {
      this.fineAmount = fineAmount;
   }

   public String getCurrency() {
      return currency;
   }

   public void setCurrency(String currency) {
      this.currency = currency;
   }

   public LocalDateTime getFineDueDate() {
      return fineDueDate;
   }

   public void setFineDueDate(LocalDateTime fineDueDate) {
      this.fineDueDate = fineDueDate;
   }

   public String getStatus() {
      return status;
   }

   public void setStatus(String status) {
      this.status = status;
   }

   public LocalDateTime getResolvedAt() {
      return resolvedAt;
   }

   public void setResolvedAt(LocalDateTime resolvedAt) {
      this.resolvedAt = resolvedAt;
   }

   public Long getResolvedBy() {
      return resolvedBy;
   }

   public void setResolvedBy(Long resolvedBy) {
      this.resolvedBy = resolvedBy;
   }

   public String getResolution() {
      return resolution;
   }

   public void setResolution(String resolution) {
      this.resolution = resolution;
   }

   public String getResolutionType() {
      return resolutionType;
   }

   public void setResolutionType(String resolutionType) {
      this.resolutionType = resolutionType;
   }

   public Boolean getFinePaid() {
      return finePaid;
   }

   public void setFinePaid(Boolean finePaid) {
      this.finePaid = finePaid;
   }

   public LocalDateTime getFinePaidAt() {
      return finePaidAt;
   }

   public void setFinePaidAt(LocalDateTime finePaidAt) {
      this.finePaidAt = finePaidAt;
   }

   public BigDecimal getFinePaidAmount() {
      return finePaidAmount;
   }

   public void setFinePaidAmount(BigDecimal finePaidAmount) {
      this.finePaidAmount = finePaidAmount;
   }

   public String getFinePaymentReference() {
      return finePaymentReference;
   }

   public void setFinePaymentReference(String finePaymentReference) {
      this.finePaymentReference = finePaymentReference;
   }

   public Boolean getNotificationSent() {
      return notificationSent;
   }

   public void setNotificationSent(Boolean notificationSent) {
      this.notificationSent = notificationSent;
   }

   public LocalDateTime getNotificationSentAt() {
      return notificationSentAt;
   }

   public void setNotificationSentAt(LocalDateTime notificationSentAt) {
      this.notificationSentAt = notificationSentAt;
   }

   public String getNotificationMethod() {
      return notificationMethod;
   }

   public void setNotificationMethod(String notificationMethod) {
      this.notificationMethod = notificationMethod;
   }

   public String getNotes() {
      return notes;
   }

   public void setNotes(String notes) {
      this.notes = notes;
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

   public LocalDateTime getDeletedAt() {
      return deletedAt;
   }

   public void setDeletedAt(LocalDateTime deletedAt) {
      this.deletedAt = deletedAt;
   }

   public Long getDeletedBy() {
      return deletedBy;
   }

   public void setDeletedBy(Long deletedBy) {
      this.deletedBy = deletedBy;
   }

   // ========================= EQUALS, HASHCODE Y TOSTRING =========================

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Infraction that = (Infraction) o;
      return Objects.equals(id, that.id);
   }

   @Override
   public int hashCode() {
      return Objects.hash(id);
   }

   @Override
   public String toString() {
      return "Infraction{" +
            "id=" + id +
            ", infractionCode='" + infractionCode + '\'' +
            ", parkingId=" + parkingId +
            ", type='" + infractionType + '\'' +
            ", severity='" + severity + '\'' +
            ", status='" + status + '\'' +
            ", hasFine=" + hasFine() +
            ", finePaid=" + isFinePaid() +
            '}';
   }
}