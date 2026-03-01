package com.winnersystems.smartparking.parking.application.dto.command;

/**
 * Command para actualizar un Customer existente.
 *
 * @author Edwin Yoner - Winner Systems
 */
public record UpdateCustomerCommand(
      String firstName,
      String lastName,
      String phone,
      String email,
      String address
) {}