package com.winnersystems.smartparking.parking.application.dto.command;

import java.time.LocalDate;
import java.util.List;

/**
 * Command para asignar operadores a una zona específica.
 *
 * Este command permite asignar múltiples operadores a una zona en un solo turno.
 * Cada operador puede tener un período de asignación específico.
 *
 * @param zoneId ID de la zona donde se asignarán los operadores
 * @param shiftId ID del turno para el cual se asignan los operadores
 * @param assignments lista de asignaciones individuales de operadores
 * @param createdBy ID del usuario que crea las asignaciones
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public record AssignOperatorsCommand(
      Long zoneId,
      Long shiftId,
      List<OperatorAssignmentData> assignments,
      Long createdBy
) {
   /**
    * Datos de asignación individual de un operador.
    *
    * @param operatorId ID del operador a asignar
    * @param startDate fecha de inicio de la asignación (si es null, usa hoy)
    * @param endDate fecha de fin de la asignación (si es null, es indefinida)
    */
   public record OperatorAssignmentData(
         Long operatorId,
         LocalDate startDate,
         LocalDate endDate
   ) {}
}