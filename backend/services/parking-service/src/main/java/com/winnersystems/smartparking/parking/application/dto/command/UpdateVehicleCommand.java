package com.winnersystems.smartparking.parking.application.dto.command;

/**
 * Command para actualizar un Vehicle existente.
 *
 * @author Edwin Yoner - Winner Systems
 */
public record UpdateVehicleCommand(
      String color,
      String brand
) {}