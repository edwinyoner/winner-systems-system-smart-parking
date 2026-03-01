package com.winnersystems.smartparking.parking.application.dto.command;

/**
 * Command para crear un nuevo CustomerVehicle (registro histórico).
 *
 * @author Edwin Yoner - Winner Systems
 */
public record CreateCustomerVehicleCommand(
      Long customerId,
      Long vehicleId
) {
   public CreateCustomerVehicleCommand {
      if (customerId == null) {
         throw new IllegalArgumentException("customerId es requerido");
      }
      if (vehicleId == null) {
         throw new IllegalArgumentException("vehicleId es requerido");
      }
   }
}