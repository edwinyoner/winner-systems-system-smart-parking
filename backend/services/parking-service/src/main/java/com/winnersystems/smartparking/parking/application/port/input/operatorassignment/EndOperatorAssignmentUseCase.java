package com.winnersystems.smartparking.parking.application.port.input.operatorassignment;

import com.winnersystems.smartparking.parking.application.dto.query.OperatorAssignmentDto;

import java.time.LocalDate;

/**
 * Caso de uso para finalizar una asignación de operador.
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public interface EndOperatorAssignmentUseCase {

   /**
    * Finaliza una asignación inmediatamente (establece endDate como hoy).
    *
    * @param assignmentId ID de la asignación a finalizar
    * @param updatedBy ID del usuario que finaliza
    * @return asignación actualizada
    * @throws IllegalArgumentException si la asignación no existe
    * @throws IllegalStateException si la asignación ya está finalizada
    */
   OperatorAssignmentDto endNow(Long assignmentId, Long updatedBy);

   /**
    * Finaliza una asignación en una fecha específica.
    *
    * @param assignmentId ID de la asignación a finalizar
    * @param endDate fecha de finalización
    * @param updatedBy ID del usuario que finaliza
    * @return asignación actualizada
    * @throws IllegalArgumentException si la asignación no existe
    * @throws IllegalArgumentException si endDate es anterior a startDate
    */
   OperatorAssignmentDto endOn(Long assignmentId, LocalDate endDate, Long updatedBy);
}