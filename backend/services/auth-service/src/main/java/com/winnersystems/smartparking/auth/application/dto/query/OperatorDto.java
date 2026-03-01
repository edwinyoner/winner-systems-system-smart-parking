package com.winnersystems.smartparking.auth.application.dto.query;

/**
 * DTO para representar un operador (usuario con rol OPERADOR).
 * Este DTO se usa para comunicación inter-microservicio con parking-service.
 *
 * Contiene solo la información necesaria para asignaciones de operadores a zonas.
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public record OperatorDto(
      Long id,
      String firstName,
      String lastName,
      String email,
      String phoneNumber,
      Boolean status
) {
   /**
    * Nombre completo del operador.
    *
    * @return firstName + lastName
    */
   public String getFullName() {
      return firstName + " " + lastName;
   }
}