package com.winnersystems.smartparking.parking.application.port.input.operatorassignment;

import com.winnersystems.smartparking.parking.application.dto.query.OperatorAssignmentDto;

import java.util.List;

/**
 * Caso de uso para listar asignaciones de operadores.
 *
 * Permite consultar asignaciones por zona, operador o turno.
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public interface ListOperatorAssignmentsUseCase {

   /**
    * Lista todas las asignaciones de operadores de una zona específica.
    *
    * @param zoneId ID de la zona
    * @return lista de asignaciones (activas y finalizadas)
    */
   List<OperatorAssignmentDto> listByZone(Long zoneId);

   /**
    * Lista todas las asignaciones activas de una zona específica.
    *
    * @param zoneId ID de la zona
    * @return lista de asignaciones activas solamente
    */
   List<OperatorAssignmentDto> listActiveByZone(Long zoneId);

   /**
    * Lista todas las asignaciones de un operador específico.
    *
    * @param operatorId ID del operador
    * @return lista de asignaciones del operador
    */
   List<OperatorAssignmentDto> listByOperator(Long operatorId);

   /**
    * Lista todas las asignaciones de un turno específico.
    *
    * @param shiftId ID del turno
    * @return lista de asignaciones del turno
    */
   List<OperatorAssignmentDto> listByShift(Long shiftId);
}