package com.winnersystems.smartparking.parking.application.port.input.operatorassignment;

import com.winnersystems.smartparking.parking.application.dto.command.AssignOperatorsCommand;
import com.winnersystems.smartparking.parking.application.dto.query.OperatorAssignmentDto;

import java.util.List;

/**
 * Caso de uso para asignar operadores a una zona.
 *
 * Permite asignar múltiples operadores a una zona específica en un turno.
 * Valida que los operadores existan en auth-service antes de crear las asignaciones.
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public interface AssignOperatorsUseCase {

   /**
    * Asigna múltiples operadores a una zona para un turno específico.
    *
    * @param command datos de la asignación
    * @return lista de asignaciones creadas
    * @throws IllegalArgumentException si la zona o turno no existen
    * @throws IllegalArgumentException si algún operador no existe o no está activo
    */
   List<OperatorAssignmentDto> assignOperators(AssignOperatorsCommand command);
}