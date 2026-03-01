package com.winnersystems.smartparking.parking.application.dto.command;

/**
 * Command para resolver una Infraction.
 *
 * @author Edwin Yoner - Winner Systems
 */
public record ResolveInfractionCommand(
      String resolutionType,  // PAID, DISMISSED, ESCALATED
      String resolution
) {
   public ResolveInfractionCommand {
      if (resolutionType == null || resolutionType.isBlank()) {
         throw new IllegalArgumentException("resolutionType es requerido");
      }
      if (resolution == null || resolution.isBlank()) {
         throw new IllegalArgumentException("resolution es requerido");
      }
   }
}