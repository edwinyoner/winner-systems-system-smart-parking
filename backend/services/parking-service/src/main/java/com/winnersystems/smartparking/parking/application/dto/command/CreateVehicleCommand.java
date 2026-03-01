package com.winnersystems.smartparking.parking.application.dto.command;

/**
 * Command para crear un nuevo Vehicle.
 *
 * @author Edwin Yoner - Winner Systems
 */
public record CreateVehicleCommand(
      String licensePlate,
      String color,
      String brand
) {
   public CreateVehicleCommand {
      if (licensePlate == null || licensePlate.isBlank()) {
         throw new IllegalArgumentException("licensePlate es requerido");
      }
   }
}