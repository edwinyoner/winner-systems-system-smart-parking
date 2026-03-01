package com.winnersystems.smartparking.parking.application.dto.command;

/**
 * Command para actualizar una Infraction existente.
 *
 * @author Edwin Yoner - Winner Systems
 */
public record UpdateInfractionCommand(
      String description,
      String severity,
      String evidence,
      String notes
) {}