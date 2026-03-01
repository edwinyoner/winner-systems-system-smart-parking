package com.winnersystems.smartparking.parking.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad de dominio que representa un PAGO realizado por una transacción.
 *
 * Cada pago está asociado a UNA transacción específica.
 * En este sistema, cada transacción tiene UN SOLO pago completo (no pagos parciales).
 *
 * El pago registra:
 * - Monto pagado (debe coincidir con Transaction.totalAmount)
 * - Método de pago usado (efectivo, tarjeta, QR, etc.)
 * - Fecha y hora del pago
 * - Operador que registró el pago
 * - Número de referencia (si el método lo requiere)
 *
 * Estados de pago:
 * - COMPLETED: Pago exitoso y completado
 * - REFUNDED: Pago devuelto al cliente
 * - CANCELLED: Pago cancelado
 *
 * Los pagos NO se eliminan (no tiene soft delete), mantienen histórico completo.
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public class Payment {

   // ========================= CAMPOS DE IDENTIDAD =========================

   private Long id;
   private Long transactionId;                            // FK a Transaction (NOT NULL, UNIQUE)
   private Long paymentTypeId;                            // FK a PaymentType (NOT NULL)

   // ========================= CAMPOS DE PAGO =========================

   private BigDecimal amount;                             // Monto pagado (NOT NULL)
   private String currency;                               // Moneda (default "PEN")
   private LocalDateTime paymentDate;                     // Fecha/hora del pago (NOT NULL)

   private String referenceNumber;                        // Número de operación/referencia (nullable)
   // Obligatorio si PaymentType.requiresReference = true

   // ========================= CAMPOS DE OPERACIÓN =========================

   private Long operatorId;                               // FK a User - Operador que cobró (NOT NULL)

   // ========================= CAMPOS DE ESTADO =========================

   private String status;                                 // COMPLETED, REFUNDED, CANCELLED

   // ========================= CAMPOS DE DEVOLUCIÓN =========================

   private BigDecimal refundAmount;                       // Monto devuelto (nullable)
   private LocalDateTime refundDate;                      // Fecha de devolución (nullable)
   private String refundReason;                           // Razón de devolución (nullable)
   private Long refundOperatorId;                         // Operador que procesó devolución (nullable)

   // ========================= CAMPOS DE OBSERVACIONES =========================

   private String notes;                                  // Notas adicionales

   // ========================= CAMPOS DE AUDITORÍA =========================

   private LocalDateTime createdAt;
   private Long createdBy;
   private LocalDateTime updatedAt;
   private Long updatedBy;

   // ========================= CONSTANTES - ESTADOS =========================

   public static final String STATUS_COMPLETED = "COMPLETED";
   public static final String STATUS_REFUNDED = "REFUNDED";
   public static final String STATUS_CANCELLED = "CANCELLED";

   // ========================= CONSTRUCTORES =========================

   /**
    * Constructor vacío - Inicializa con valores por defecto.
    */
   public Payment() {
      this.status = STATUS_COMPLETED;
      this.currency = "PEN";
      this.paymentDate = LocalDateTime.now();
      this.createdAt = LocalDateTime.now();
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Constructor con campos básicos.
    *
    * @param transactionId ID de la transacción
    * @param paymentTypeId ID del tipo de pago
    * @param amount monto pagado
    * @param operatorId operador que registra
    */
   public Payment(Long transactionId, Long paymentTypeId, BigDecimal amount, Long operatorId) {
      this();
      this.transactionId = transactionId;
      this.paymentTypeId = paymentTypeId;
      this.amount = amount;
      this.operatorId = operatorId;
   }

   /**
    * Constructor completo con número de referencia.
    *
    * @param transactionId ID de la transacción
    * @param paymentTypeId ID del tipo de pago
    * @param amount monto pagado
    * @param referenceNumber número de referencia
    * @param operatorId operador que registra
    */
   public Payment(Long transactionId, Long paymentTypeId, BigDecimal amount,
                  String referenceNumber, Long operatorId) {
      this(transactionId, paymentTypeId, amount, operatorId);
      this.referenceNumber = referenceNumber;
   }

   // ========================= MÉTODOS DE NEGOCIO - DEVOLUCIÓN =========================

   /**
    * Procesa una devolución completa del pago.
    *
    * @param reason razón de la devolución
    * @param operatorId operador que procesa
    */
   public void processRefund(String reason, Long operatorId) {
      this.refundAmount = this.amount;
      this.refundDate = LocalDateTime.now();
      this.refundReason = reason;
      this.refundOperatorId = operatorId;
      this.status = STATUS_REFUNDED;
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Procesa una devolución parcial del pago.
    *
    * @param amount monto a devolver
    * @param reason razón de la devolución
    * @param operatorId operador que procesa
    */
   public void processPartialRefund(BigDecimal amount, String reason, Long operatorId) {
      this.refundAmount = amount;
      this.refundDate = LocalDateTime.now();
      this.refundReason = reason;
      this.refundOperatorId = operatorId;
      this.status = STATUS_REFUNDED;
      this.updatedAt = LocalDateTime.now();
   }

   // OPCIONAL: Método para validar que el monto coincida con Transaction
   public boolean matchesTransactionAmount(BigDecimal transactionAmount) {
      return amount != null && amount.compareTo(transactionAmount) == 0;
   }

   // OPCIONAL: Verificar si requiere número de referencia
   public boolean requiresReferenceNumber() {
      // Esto requiere info de PaymentType, pero puedes agregarlo después
      // cuando hagas los casos de uso
      return false; // Placeholder
   }

   // ========================= MÉTODOS DE NEGOCIO - CANCELACIÓN =========================

   /**
    * Cancela el pago.
    *
    * @param reason razón de cancelación
    */
   public void cancel(String reason) {
      this.status = STATUS_CANCELLED;
      this.notes = reason;
      this.updatedAt = LocalDateTime.now();
   }

   // ========================= MÉTODOS DE CONSULTA - ESTADOS =========================

   /**
    * Verifica si el pago está completado.
    *
    * @return true si status = COMPLETED
    */
   public boolean isCompleted() {
      return STATUS_COMPLETED.equals(status);
   }

   /**
    * Verifica si el pago fue devuelto.
    *
    * @return true si status = REFUNDED
    */
   public boolean isRefunded() {
      return STATUS_REFUNDED.equals(status);
   }

   /**
    * Verifica si el pago fue cancelado.
    *
    * @return true si status = CANCELLED
    */
   public boolean isCancelled() {
      return STATUS_CANCELLED.equals(status);
   }

   /**
    * Verifica si tiene devolución registrada.
    *
    * @return true si refundAmount no es null
    */
   public boolean hasRefund() {
      return refundAmount != null && refundAmount.compareTo(BigDecimal.ZERO) > 0;
   }

   /**
    * Verifica si la devolución es completa.
    *
    * @return true si refundAmount = amount
    */
   public boolean isFullRefund() {
      return hasRefund() && refundAmount.compareTo(amount) == 0;
   }

   /**
    * Verifica si la devolución es parcial.
    *
    * @return true si refundAmount < amount
    */
   public boolean isPartialRefund() {
      return hasRefund() && refundAmount.compareTo(amount) < 0;
   }

   // ========================= MÉTODOS DE CONSULTA - INFORMACIÓN =========================

   /**
    * Obtiene el monto neto (pagado - devuelto).
    *
    * @return monto neto
    */
   public BigDecimal getNetAmount() {
      if (refundAmount == null) {
         return amount;
      }
      return amount.subtract(refundAmount);
   }

   /**
    * Obtiene una descripción del pago.
    *
    * @return descripción formateada
    */
   public String getDescription() {
      return "Payment #" + id + " [" + status + "] " + currency + " " + amount;
   }

   // ========================= MÉTODOS DE VALIDACIÓN =========================

   /**
    * Verifica si el pago tiene número de referencia.
    *
    * @return true si referenceNumber no es null
    */
   public boolean hasReferenceNumber() {
      return referenceNumber != null && !referenceNumber.trim().isEmpty();
   }

   /**
    * Verifica si tiene datos completos.
    *
    * @return true si tiene todos los campos requeridos
    */
   public boolean isValid() {
      return transactionId != null && paymentTypeId != null &&
            amount != null && amount.compareTo(BigDecimal.ZERO) > 0 &&
            operatorId != null && paymentDate != null;
   }

   // ========================= GETTERS Y SETTERS =========================

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public Long getTransactionId() {
      return transactionId;
   }

   public void setTransactionId(Long transactionId) {
      this.transactionId = transactionId;
   }

   public Long getPaymentTypeId() {
      return paymentTypeId;
   }

   public void setPaymentTypeId(Long paymentTypeId) {
      this.paymentTypeId = paymentTypeId;
   }

   public BigDecimal getAmount() {
      return amount;
   }

   public void setAmount(BigDecimal amount) {
      this.amount = amount;
   }

   public String getCurrency() {
      return currency;
   }

   public void setCurrency(String currency) {
      this.currency = currency;
   }

   public LocalDateTime getPaymentDate() {
      return paymentDate;
   }

   public void setPaymentDate(LocalDateTime paymentDate) {
      this.paymentDate = paymentDate;
   }

   public String getReferenceNumber() {
      return referenceNumber;
   }

   public void setReferenceNumber(String referenceNumber) {
      this.referenceNumber = referenceNumber;
   }

   public Long getOperatorId() {
      return operatorId;
   }

   public void setOperatorId(Long operatorId) {
      this.operatorId = operatorId;
   }

   public String getStatus() {
      return status;
   }

   public void setStatus(String status) {
      this.status = status;
   }

   public BigDecimal getRefundAmount() {
      return refundAmount;
   }

   public void setRefundAmount(BigDecimal refundAmount) {
      this.refundAmount = refundAmount;
   }

   public LocalDateTime getRefundDate() {
      return refundDate;
   }

   public void setRefundDate(LocalDateTime refundDate) {
      this.refundDate = refundDate;
   }

   public String getRefundReason() {
      return refundReason;
   }

   public void setRefundReason(String refundReason) {
      this.refundReason = refundReason;
   }

   public Long getRefundOperatorId() {
      return refundOperatorId;
   }

   public void setRefundOperatorId(Long refundOperatorId) {
      this.refundOperatorId = refundOperatorId;
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

   // ========================= EQUALS, HASHCODE Y TOSTRING =========================

   /**
    * Dos pagos son iguales si tienen el mismo ID.
    */
   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Payment payment = (Payment) o;
      return Objects.equals(id, payment.id);
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
      return "Payment{" +
            "id=" + id +
            ", transactionId=" + transactionId +
            ", paymentTypeId=" + paymentTypeId +
            ", amount=" + amount +
            ", currency='" + currency + '\'' +
            ", status='" + status + '\'' +
            ", hasRefund=" + hasRefund() +
            ", paymentDate=" + paymentDate +
            '}';
   }
}