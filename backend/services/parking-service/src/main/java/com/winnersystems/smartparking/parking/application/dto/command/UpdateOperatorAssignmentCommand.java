package com.winnersystems.smartparking.parking.application.dto.command;

import java.time.LocalDate;

/**
 * Command para actualizar las fechas de una asignación de operador.
 *
 * Permite extender, acortar o hacer indefinida una asignación existente.
 *
 * @param assignmentId ID de la asignación a actualizar
 * @param startDate nueva fecha de inicio (si es null, mantiene la actual)
 * @param endDate nueva fecha de fin (si es null, hace la asignación indefinida)
 * @param updatedBy ID del usuario que actualiza
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public record UpdateOperatorAssignmentCommand(
      Long assignmentId,
      LocalDate startDate,
      LocalDate endDate,
      Long updatedBy
) {}