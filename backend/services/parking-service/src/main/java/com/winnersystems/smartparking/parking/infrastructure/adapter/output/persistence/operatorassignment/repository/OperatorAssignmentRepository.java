package com.winnersystems.smartparking.parking.infrastructure.adapter.output.persistence.operatorassignment.repository;

import com.winnersystems.smartparking.parking.infrastructure.adapter.output.persistence.operatorassignment.entity.OperatorAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio JPA para OperatorAssignmentEntity.
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
@Repository
public interface OperatorAssignmentRepository extends JpaRepository<OperatorAssignmentEntity, Long> {

   /**
    * Lista todas las asignaciones de una zona específica.
    *
    * @param zoneId ID de la zona
    * @return lista de asignaciones
    */
   @Query("SELECT oa FROM OperatorAssignmentEntity oa WHERE oa.zone.id = :zoneId ORDER BY oa.startDate DESC")
   List<OperatorAssignmentEntity> findByZoneId(@Param("zoneId") Long zoneId);

   /**
    * Lista todas las asignaciones activas de una zona específica.
    * Una asignación es activa si startDate <= hoy Y (endDate IS NULL O endDate > hoy).
    *
    * @param zoneId ID de la zona
    * @param today fecha actual
    * @return lista de asignaciones activas
    */
   @Query("""
         SELECT oa FROM OperatorAssignmentEntity oa 
         WHERE oa.zone.id = :zoneId 
         AND oa.startDate <= :today 
         AND (oa.endDate IS NULL OR oa.endDate > :today)
         ORDER BY oa.startDate DESC
         """)
   List<OperatorAssignmentEntity> findActiveByZoneId(
         @Param("zoneId") Long zoneId,
         @Param("today") LocalDate today
   );

   /**
    * Lista todas las asignaciones de un operador específico.
    *
    * @param operatorId ID del operador
    * @return lista de asignaciones
    */
   @Query("SELECT oa FROM OperatorAssignmentEntity oa WHERE oa.operatorId = :operatorId ORDER BY oa.startDate DESC")
   List<OperatorAssignmentEntity> findByOperatorId(@Param("operatorId") Long operatorId);

   /**
    * Lista todas las asignaciones de un turno específico.
    *
    * @param shiftId ID del turno
    * @return lista de asignaciones
    */
   @Query("SELECT oa FROM OperatorAssignmentEntity oa WHERE oa.shift.id = :shiftId ORDER BY oa.startDate DESC")
   List<OperatorAssignmentEntity> findByShiftId(@Param("shiftId") Long shiftId);

   /**
    * Verifica si existe una asignación activa para un operador en una zona y turno específicos.
    *
    * @param operatorId ID del operador
    * @param zoneId ID de la zona
    * @param shiftId ID del turno
    * @param today fecha actual
    * @return true si existe asignación activa
    */
   @Query("""
         SELECT CASE WHEN COUNT(oa) > 0 THEN true ELSE false END 
         FROM OperatorAssignmentEntity oa 
         WHERE oa.operatorId = :operatorId 
         AND oa.zone.id = :zoneId 
         AND oa.shift.id = :shiftId 
         AND oa.startDate <= :today 
         AND (oa.endDate IS NULL OR oa.endDate > :today)
         """)
   boolean existsActiveAssignment(
         @Param("operatorId") Long operatorId,
         @Param("zoneId") Long zoneId,
         @Param("shiftId") Long shiftId,
         @Param("today") LocalDate today
   );
}