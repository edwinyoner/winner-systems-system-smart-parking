package com.winnersystems.smartparking.parking.application.service.operatorassignment;

import com.winnersystems.smartparking.parking.application.dto.command.AssignOperatorsCommand;
import com.winnersystems.smartparking.parking.application.dto.command.UpdateOperatorAssignmentCommand;
import com.winnersystems.smartparking.parking.application.dto.query.OperatorAssignmentDetailDto;
import com.winnersystems.smartparking.parking.application.dto.query.OperatorAssignmentDto;
import com.winnersystems.smartparking.parking.application.port.input.operatorassignment.*;
import com.winnersystems.smartparking.parking.application.port.output.*;
import com.winnersystems.smartparking.parking.domain.model.OperatorAssignment;
import com.winnersystems.smartparking.parking.domain.model.Shift;
import com.winnersystems.smartparking.parking.domain.model.Zone;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para gestión de asignaciones de operadores.
 *
 * Implementa todos los casos de uso relacionados con OperatorAssignment.
 * Este servicio NO llama a auth-service directamente, esa responsabilidad
 * está en la capa de Infrastructure (REST Adapter).
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OperatorAssignmentService implements
      AssignOperatorsUseCase,
      ListOperatorAssignmentsUseCase,
      GetOperatorAssignmentUseCase,
      EndOperatorAssignmentUseCase,
      UpdateOperatorAssignmentUseCase {

   // ========================= PUERTOS DE SALIDA =========================

   private final OperatorAssignmentPersistencePort operatorAssignmentPersistencePort;
   private final ZonePersistencePort zonePersistencePort;
   private final ShiftPersistencePort shiftPersistencePort;

   // ========================= ASSIGN OPERATORS =========================

   @Override
   public List<OperatorAssignmentDto> assignOperators(AssignOperatorsCommand command) {
      log.debug("Asignando {} operadores a zona {} en turno {}",
            command.assignments().size(), command.zoneId(), command.shiftId());

      // 1. Validar que la zona existe
      Zone zone = zonePersistencePort.findById(command.zoneId())
            .orElseThrow(() -> new IllegalArgumentException(
                  "Zona no encontrada con ID: " + command.zoneId()));

      // 2. Validar que el turno existe
      Shift shift = shiftPersistencePort.findById(command.shiftId())
            .orElseThrow(() -> new IllegalArgumentException(
                  "Turno no encontrado con ID: " + command.shiftId()));

      // 3. Crear asignaciones
      List<OperatorAssignment> assignments = command.assignments().stream()
            .map(data -> {
               // Verificar si ya existe asignación activa
               boolean exists = operatorAssignmentPersistencePort.existsActiveAssignment(
                     data.operatorId(), command.zoneId(), command.shiftId());

               if (exists) {
                  log.warn("Operador {} ya tiene asignación activa en zona {} turno {}",
                        data.operatorId(), command.zoneId(), command.shiftId());
                  throw new IllegalArgumentException(
                        "El operador ya tiene una asignación activa en esta zona y turno");
               }

               // Crear asignación
               LocalDate startDate = data.startDate() != null ? data.startDate() : LocalDate.now();
               OperatorAssignment assignment = new OperatorAssignment(
                     data.operatorId(),
                     command.zoneId(),
                     command.shiftId(),
                     startDate,
                     data.endDate()
               );
               assignment.setCreatedBy(command.createdBy());
               return assignment;
            })
            .collect(Collectors.toList());

      // 4. Guardar todas las asignaciones
      List<OperatorAssignment> savedAssignments =
            operatorAssignmentPersistencePort.saveAll(assignments);

      log.debug("{} asignaciones creadas exitosamente", savedAssignments.size());

      // 5. Mapear a DTOs simplificados
      return savedAssignments.stream()
            .map(a -> new OperatorAssignmentDto(
                  a.getId(),
                  a.getOperatorId(),
                  null,  // operatorName - se cargará en Infrastructure
                  a.getZoneId(),
                  zone.getName(),
                  a.getShiftId(),
                  shift.getName(),
                  a.getStartDate(),
                  a.getEndDate(),
                  a.getStatus(),
                  a.getCreatedAt()
            ))
            .collect(Collectors.toList());
   }

   // ========================= LIST ASSIGNMENTS =========================

   @Override
   public List<OperatorAssignmentDto> listByZone(Long zoneId) {
      log.debug("Listando todas las asignaciones de zona {}", zoneId);

      List<OperatorAssignment> assignments = operatorAssignmentPersistencePort.findByZoneId(zoneId);

      return mapToSimpleDtos(assignments);
   }

   @Override
   public List<OperatorAssignmentDto> listActiveByZone(Long zoneId) {
      log.debug("Listando asignaciones activas de zona {}", zoneId);

      List<OperatorAssignment> assignments = operatorAssignmentPersistencePort.findActiveByZoneId(zoneId);

      return mapToSimpleDtos(assignments);
   }

   @Override
   public List<OperatorAssignmentDto> listByOperator(Long operatorId) {
      log.debug("Listando asignaciones del operador {}", operatorId);

      List<OperatorAssignment> assignments = operatorAssignmentPersistencePort.findByOperatorId(operatorId);

      return mapToSimpleDtos(assignments);
   }

   @Override
   public List<OperatorAssignmentDto> listByShift(Long shiftId) {
      log.debug("Listando asignaciones del turno {}", shiftId);

      List<OperatorAssignment> assignments = operatorAssignmentPersistencePort.findByShiftId(shiftId);

      return mapToSimpleDtos(assignments);
   }

   // ========================= GET ASSIGNMENT =========================

   @Override
   public OperatorAssignmentDetailDto getAssignment(Long assignmentId) {
      log.debug("🔍 Obteniendo asignación {}", assignmentId);

      OperatorAssignment assignment = operatorAssignmentPersistencePort.findById(assignmentId)
            .orElseThrow(() -> new IllegalArgumentException(
                  "Asignación no encontrada con ID: " + assignmentId));

      // Cargar entidades relacionadas
      Zone zone = zonePersistencePort.findById(assignment.getZoneId()).orElseThrow();
      Shift shift = shiftPersistencePort.findById(assignment.getShiftId()).orElseThrow();

      // La información del operador se cargará en Infrastructure desde auth-service
      return new OperatorAssignmentDetailDto(
            assignment.getId(),
            assignment.getStartDate(),
            assignment.getEndDate(),
            assignment.getStatus(),
            assignment.getDurationInDays(),
            // OperatorInfo - se completará en Infrastructure
            new OperatorAssignmentDetailDto.OperatorInfo(
                  assignment.getOperatorId(), null, null, null, null, null),
            // ZoneInfo
            new OperatorAssignmentDetailDto.ZoneInfo(
                  zone.getId(), zone.getName(), zone.getCode()),
            // ShiftInfo
            new OperatorAssignmentDetailDto.ShiftInfo(
                  shift.getId(), shift.getName(),
                  shift.getStartTime().toString(), shift.getEndTime().toString()),
            assignment.getCreatedAt(),
            assignment.getCreatedBy(),
            assignment.getUpdatedAt(),
            assignment.getUpdatedBy()
      );
   }

   // ========================= END ASSIGNMENT =========================

   @Override
   public OperatorAssignmentDto endNow(Long assignmentId, Long updatedBy) {
      log.debug("Finalizando asignación {} inmediatamente", assignmentId);

      OperatorAssignment assignment = operatorAssignmentPersistencePort.findById(assignmentId)
            .orElseThrow(() -> new IllegalArgumentException(
                  "Asignación no encontrada con ID: " + assignmentId));

      if (assignment.hasEnded()) {
         throw new IllegalStateException("La asignación ya está finalizada");
      }

      assignment.endAssignment();
      assignment.setUpdatedBy(updatedBy);

      OperatorAssignment updated = operatorAssignmentPersistencePort.save(assignment);

      log.debug("Asignación finalizada");

      return mapToSimpleDto(updated);
   }

   @Override
   public OperatorAssignmentDto endOn(Long assignmentId, LocalDate endDate, Long updatedBy) {
      log.debug("Finalizando asignación {} en fecha {}", assignmentId, endDate);

      OperatorAssignment assignment = operatorAssignmentPersistencePort.findById(assignmentId)
            .orElseThrow(() -> new IllegalArgumentException(
                  "Asignación no encontrada con ID: " + assignmentId));

      assignment.endAssignmentOn(endDate);
      assignment.setUpdatedBy(updatedBy);

      OperatorAssignment updated = operatorAssignmentPersistencePort.save(assignment);

      log.debug("Asignación finalizada en {}", endDate);

      return mapToSimpleDto(updated);
   }

   // ========================= UPDATE ASSIGNMENT =========================

   @Override
   public OperatorAssignmentDto updateAssignment(UpdateOperatorAssignmentCommand command) {
      log.debug("Actualizando asignación {}", command.assignmentId());

      OperatorAssignment assignment = operatorAssignmentPersistencePort.findById(command.assignmentId())
            .orElseThrow(() -> new IllegalArgumentException(
                  "Asignación no encontrada con ID: " + command.assignmentId()));

      // Actualizar fechas si se especifican
      if (command.startDate() != null) {
         assignment.setStartDate(command.startDate());
      }

      if (command.endDate() != null) {
         assignment.setEndDate(command.endDate());
      } else {
         // Si endDate es null explícitamente, hacer indefinida
         assignment.makeIndefinite();
      }

      // Validar fechas
      if (!assignment.hasValidDates()) {
         throw new IllegalArgumentException(
               "Las fechas son inválidas: endDate no puede ser anterior a startDate");
      }

      assignment.setUpdatedBy(command.updatedBy());

      OperatorAssignment updated = operatorAssignmentPersistencePort.save(assignment);

      log.debug("Asignación actualizada");

      return mapToSimpleDto(updated);
   }

   // ========================= HELPER METHODS =========================

   private List<OperatorAssignmentDto> mapToSimpleDtos(List<OperatorAssignment> assignments) {
      return assignments.stream()
            .map(this::mapToSimpleDto)
            .collect(Collectors.toList());
   }

   private OperatorAssignmentDto mapToSimpleDto(OperatorAssignment assignment) {
      // Cargar zona y turno
      Zone zone = zonePersistencePort.findById(assignment.getZoneId()).orElse(null);
      Shift shift = shiftPersistencePort.findById(assignment.getShiftId()).orElse(null);

      return new OperatorAssignmentDto(
            assignment.getId(),
            assignment.getOperatorId(),
            null,  // operatorName - se cargará en Infrastructure
            assignment.getZoneId(),
            zone != null ? zone.getName() : null,
            assignment.getShiftId(),
            shift != null ? shift.getName() : null,
            assignment.getStartDate(),
            assignment.getEndDate(),
            assignment.getStatus(),
            assignment.getCreatedAt()
      );
   }
}