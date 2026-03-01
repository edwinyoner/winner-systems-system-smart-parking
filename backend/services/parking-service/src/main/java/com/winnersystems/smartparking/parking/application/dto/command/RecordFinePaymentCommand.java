package com.winnersystems.smartparking.parking.application.dto.command;

import java.math.BigDecimal;

/**
 * Command para registrar pago de multa.
 *
 * @author Edwin Yoner - Winner Systems
 */
public record RecordFinePaymentCommand(
      BigDecimal amount,
      String reference
) {
   public RecordFinePaymentCommand {
      if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
         throw new IllegalArgumentException("amount debe ser mayor a 0");
      }
   }
}