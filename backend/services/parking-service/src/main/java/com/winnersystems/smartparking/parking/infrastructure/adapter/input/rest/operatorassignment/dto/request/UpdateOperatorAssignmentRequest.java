package com.winnersystems.smartparking.parking.infrastructure.adapter.input.rest.operatorassignment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO para actualizar una asignación de operador.
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOperatorAssignmentRequest {

   private LocalDate startDate;  // Opcional - si es null, mantiene la actual

   private LocalDate endDate;    // Opcional - si es null, hace indefinida
}