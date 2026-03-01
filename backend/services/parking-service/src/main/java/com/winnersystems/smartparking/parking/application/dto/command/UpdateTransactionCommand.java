package com.winnersystems.smartparking.parking.application.dto.command;

/**
 * Command para actualizar notas/observaciones de una Transaction.
 *
 * @author Edwin Yoner - Winner Systems
 */
public record UpdateTransactionCommand(
      String notes
) {}