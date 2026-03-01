package com.winnersystems.smartparking.parking.application.dto.command;

/**
 * Command para crear un nuevo Customer.
 *
 * @author Edwin Yoner - Winner Systems
 */
public record CreateCustomerCommand(
      Long documentTypeId,
      String documentNumber,
      String firstName,
      String lastName,
      String phone,
      String email,
      String address
) {
   // Validaciones en constructor compacto (opcional)
   public CreateCustomerCommand {
      if (documentTypeId == null) {
         throw new IllegalArgumentException("documentTypeId es requerido");
      }
      if (documentNumber == null || documentNumber.isBlank()) {
         throw new IllegalArgumentException("documentNumber es requerido");
      }
      if (firstName == null || firstName.isBlank()) {
         throw new IllegalArgumentException("firstName es requerido");
      }
      if (lastName == null || lastName.isBlank()) {
         throw new IllegalArgumentException("lastName es requerido");
      }
   }
}