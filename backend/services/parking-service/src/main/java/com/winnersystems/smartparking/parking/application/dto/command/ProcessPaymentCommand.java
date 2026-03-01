package com.winnersystems.smartparking.parking.application.dto.command;

import java.math.BigDecimal;

/**
 * Comando para procesar el PAGO de una transacción de estacionamiento.
 *
 * Este comando registra el pago realizado, actualiza el estado de la Transaction
 * a PAID y opcionalmente envía el comprobante digital.
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public record ProcessPaymentCommand(
      // ========================= IDENTIFICACIÓN =========================
      Long transactionId,              // ID de la transacción (NOT NULL)

      // ========================= PAGO =========================
      Long paymentTypeId,              // ID del tipo de pago (NOT NULL)
      BigDecimal amountPaid,           // Monto pagado (NOT NULL)
      String referenceNumber,          // Número de referencia (opcional, depende del método)

      // ========================= REGISTRO =========================
      Long operatorId,                 // Operador que cobra (NOT NULL)

      // ========================= COMPROBANTE =========================
      Boolean sendReceipt,             // ¿Enviar comprobante? (default true si null)

      // ========================= OBSERVACIONES =========================
      String notes                     // Notas adicionales (opcional)
) {
   // Validaciones en constructor compacto
   public ProcessPaymentCommand {
      if (transactionId == null) {
         throw new IllegalArgumentException("transactionId es requerido");
      }
      if (paymentTypeId == null) {
         throw new IllegalArgumentException("paymentTypeId es requerido");
      }
      if (amountPaid == null || amountPaid.compareTo(BigDecimal.ZERO) <= 0) {
         throw new IllegalArgumentException("amountPaid debe ser mayor a 0");
      }
      if (operatorId == null) {
         throw new IllegalArgumentException("operatorId es requerido");
      }

      // Default para sendReceipt
      if (sendReceipt == null) {
         sendReceipt = true;
      }
   }

   /**
    * Factory method para crear comando sin comprobante.
    */
   public static ProcessPaymentCommand withoutReceipt(
         Long transactionId,
         Long paymentTypeId,
         BigDecimal amountPaid,
         Long operatorId
   ) {
      return new ProcessPaymentCommand(
            transactionId,
            paymentTypeId,
            amountPaid,
            null,
            operatorId,
            false,
            null
      );
   }
}