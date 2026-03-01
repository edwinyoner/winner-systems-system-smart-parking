package com.winnersystems.smartparking.parking.infrastructure.adapter.input.rest.operatorassignment.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO para asignar operadores a una zona.
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignOperatorsRequest {

   @NotNull(message = "El ID de la zona es obligatorio")
   private Long zoneId;

   @NotNull(message = "El ID del turno es obligatorio")
   private Long shiftId;

   @NotEmpty(message = "Debe asignar al menos un operador")
   @Valid
   private List<OperatorAssignmentData> assignments;

   /**
    * Datos de asignación individual de un operador.
    */
   @Data
   @NoArgsConstructor
   @AllArgsConstructor
   public static class OperatorAssignmentData {

      @NotNull(message = "El ID del operador es obligatorio")
      private Long operatorId;

      private LocalDate startDate;  // Opcional - si es null, usa hoy

      private LocalDate endDate;    // Opcional - si es null, es indefinida
   }
}