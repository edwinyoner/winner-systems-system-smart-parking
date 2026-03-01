package com.winnersystems.smartparking.parking.application.dto.command;

import java.math.BigDecimal;

/**
 * Command para procesar devolución de un Payment.
 *
 * @author Edwin Yoner - Winner Systems
 */
public record RefundPaymentCommand(
      BigDecimal refundAmount,
      String reason
) {
   public RefundPaymentCommand {
      if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
         throw new IllegalArgumentException("refundAmount debe ser mayor a 0");
      }
      if (reason == null || reason.isBlank()) {
         throw new IllegalArgumentException("reason es requerido");
      }
   }
}