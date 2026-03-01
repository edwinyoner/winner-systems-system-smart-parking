package com.winnersystems.smartparking.parking.application.port.input.operatorassignment;

import com.winnersystems.smartparking.parking.application.dto.command.UpdateOperatorAssignmentCommand;
import com.winnersystems.smartparking.parking.application.dto.query.OperatorAssignmentDto;

/**
 * Caso de uso para actualizar las fechas de una asignación de operador.
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public interface UpdateOperatorAssignmentUseCase {

   /**
    * Actualiza las fechas de una asignación existente.
    *
    * @param command datos de actualización
    * @return asignación actualizada
    * @throws IllegalArgumentException si la asignación no existe
    * @throws IllegalArgumentException si las fechas son inválidas
    */
   OperatorAssignmentDto updateAssignment(UpdateOperatorAssignmentCommand command);
}