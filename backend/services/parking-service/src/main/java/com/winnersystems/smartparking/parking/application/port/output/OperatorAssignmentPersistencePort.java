package com.winnersystems.smartparking.parking.application.port.output;

import com.winnersystems.smartparking.parking.domain.model.OperatorAssignment;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistencia de asignaciones de operadores.
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public interface OperatorAssignmentPersistencePort {

   /**
    * Guarda una asignación de operador.
    *
    * @param assignment asignación a guardar
    * @return asignación guardada con ID generado
    */
   OperatorAssignment save(OperatorAssignment assignment);

   /**
    * Guarda múltiples asignaciones en batch.
    *
    * @param assignments lista de asignaciones a guardar
    * @return lista de asignaciones guardadas
    */
   List<OperatorAssignment> saveAll(List<OperatorAssignment> assignments);

   /**
    * Busca una asignación por ID.
    *
    * @param id ID de la asignación
    * @return Optional con la asignación si existe
    */
   Optional<OperatorAssignment> findById(Long id);

   /**
    * Lista todas las asignaciones de una zona.
    *
    * @param zoneId ID de la zona
    * @return lista de asignaciones
    */
   List<OperatorAssignment> findByZoneId(Long zoneId);

   /**
    * Lista todas las asignaciones activas de una zona.
    *
    * @param zoneId ID de la zona
    * @return lista de asignaciones activas
    */
   List<OperatorAssignment> findActiveByZoneId(Long zoneId);

   /**
    * Lista todas las asignaciones de un operador.
    *
    * @param operatorId ID del operador
    * @return lista de asignaciones
    */
   List<OperatorAssignment> findByOperatorId(Long operatorId);

   /**
    * Lista todas las asignaciones de un turno.
    *
    * @param shiftId ID del turno
    * @return lista de asignaciones
    */
   List<OperatorAssignment> findByShiftId(Long shiftId);

   /**
    * Verifica si existe una asignación activa para un operador en una zona específica.
    *
    * @param operatorId ID del operador
    * @param zoneId ID de la zona
    * @param shiftId ID del turno
    * @return true si existe asignación activa, false si no
    */
   boolean existsActiveAssignment(Long operatorId, Long zoneId, Long shiftId);

   /**
    * Elimina una asignación por ID (hard delete).
    * Usar solo en caso de corrección de errores de captura.
    *
    * @param id ID de la asignación a eliminar
    */
   void deleteById(Long id);
}