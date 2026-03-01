package com.winnersystems.smartparking.parking.application.dto.command;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Command para asignar multa a una Infraction.
 *
 * @author Edwin Yoner - Winner Systems
 */
public record AssignFineCommand(
      BigDecimal fineAmount,
      LocalDateTime fineDueDate
) {
   public AssignFineCommand {
      if (fineAmount == null || fineAmount.compareTo(BigDecimal.ZERO) <= 0) {
         throw new IllegalArgumentException("fineAmount debe ser mayor a 0");
      }
   }
}