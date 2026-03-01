package com.winnersystems.smartparking.parking.application.dto.command;

/**
 * Command para crear una nueva Infraction.
 *
 * @author Edwin Yoner - Winner Systems
 */
public record CreateInfractionCommand(
      Long parkingId,
      Long vehicleId,
      Long zoneId,
      Long spaceId,
      Long transactionId,
      Long customerId,
      String infractionType,
      String severity,
      String description,
      String evidence
) {
   public CreateInfractionCommand {
      if (parkingId == null) {
         throw new IllegalArgumentException("parkingId es requerido");
      }
      if (vehicleId == null) {
         throw new IllegalArgumentException("vehicleId es requerido");
      }
      if (zoneId == null) {
         throw new IllegalArgumentException("zoneId es requerido");
      }
      if (infractionType == null || infractionType.isBlank()) {
         throw new IllegalArgumentException("infractionType es requerido");
      }
   }
}