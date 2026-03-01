package com.winnersystems.smartparking.parking.application.dto.command;

/**
 * Command para cancelar una Transaction.
 *
 * @author Edwin Yoner - Winner Systems
 */
public record CancelTransactionCommand(
      String reason
) {
   public CancelTransactionCommand {
      if (reason == null || reason.isBlank()) {
         throw new IllegalArgumentException("reason es requerido");
      }
   }
}