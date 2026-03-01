package com.winnersystems.smartparking.parking.application.port.input.operatorassignment;

import com.winnersystems.smartparking.parking.application.dto.query.OperatorAssignmentDetailDto;

/**
 * Caso de uso para obtener una asignación específica con todos sus detalles.
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public interface GetOperatorAssignmentUseCase {

   /**
    * Obtiene una asignación específica con información completa.
    *
    * @param assignmentId ID de la asignación
    * @return asignación con todos sus detalles
    * @throws IllegalArgumentException si la asignación no existe
    */
   OperatorAssignmentDetailDto getAssignment(Long assignmentId);
}