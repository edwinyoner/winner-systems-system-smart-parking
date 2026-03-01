package com.winnersystems.smartparking.parking.infrastructure.adapter.output.persistence.operatorassignment;

import com.winnersystems.smartparking.parking.application.port.output.OperatorAssignmentPersistencePort;
import com.winnersystems.smartparking.parking.domain.model.OperatorAssignment;
import com.winnersystems.smartparking.parking.infrastructure.adapter.output.persistence.operatorassignment.entity.OperatorAssignmentEntity;
import com.winnersystems.smartparking.parking.infrastructure.adapter.output.persistence.operatorassignment.mapper.OperatorAssignmentPersistenceMapper;
import com.winnersystems.smartparking.parking.infrastructure.adapter.output.persistence.operatorassignment.repository.OperatorAssignmentRepository;
import com.winnersystems.smartparking.parking.infrastructure.adapter.output.persistence.shift.entity.ShiftEntity;
import com.winnersystems.smartparking.parking.infrastructure.adapter.output.persistence.shift.repository.ShiftRepository;
import com.winnersystems.smartparking.parking.infrastructure.adapter.output.persistence.zone.entity.ZoneEntity;
import com.winnersystems.smartparking.parking.infrastructure.adapter.output.persistence.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para OperatorAssignment.
 *
 * Implementa el puerto de salida OperatorAssignmentPersistencePort.
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class OperatorAssignmentPersistenceAdapter implements OperatorAssignmentPersistencePort {

   private final OperatorAssignmentRepository repository;
   private final ZoneRepository zoneRepository;
   private final ShiftRepository shiftRepository;
   private final OperatorAssignmentPersistenceMapper mapper;

   @Override
   public OperatorAssignment save(OperatorAssignment assignment) {
      OperatorAssignmentEntity entity = mapper.toEntity(assignment);

      // Cargar entidades relacionadas desde la BD (managed entities)
      if (entity.getZone() != null && entity.getZone().getId() != null) {
         ZoneEntity managedZone = zoneRepository.findById(entity.getZone().getId())
               .orElseThrow(() -> new IllegalArgumentException(
                     "Zona no encontrada: " + entity.getZone().getId()));
         entity.setZone(managedZone);
      }

      if (entity.getShift() != null && entity.getShift().getId() != null) {
         ShiftEntity managedShift = shiftRepository.findById(entity.getShift().getId())
               .orElseThrow(() -> new IllegalArgumentException(
                     "Turno no encontrado: " + entity.getShift().getId()));
         entity.setShift(managedShift);
      }

      OperatorAssignmentEntity savedEntity = repository.save(entity);
      return mapper.toDomain(savedEntity);
   }

   @Override
   public List<OperatorAssignment> saveAll(List<OperatorAssignment> assignments) {
      List<OperatorAssignmentEntity> entities = assignments.stream()
            .map(assignment -> {
               OperatorAssignmentEntity entity = mapper.toEntity(assignment);

               // Cargar relaciones managed
               if (entity.getZone() != null && entity.getZone().getId() != null) {
                  ZoneEntity managedZone = zoneRepository.findById(entity.getZone().getId())
                        .orElseThrow(() -> new IllegalArgumentException(
                              "Zona no encontrada: " + entity.getZone().getId()));
                  entity.setZone(managedZone);
               }

               if (entity.getShift() != null && entity.getShift().getId() != null) {
                  ShiftEntity managedShift = shiftRepository.findById(entity.getShift().getId())
                        .orElseThrow(() -> new IllegalArgumentException(
                              "Turno no encontrado: " + entity.getShift().getId()));
                  entity.setShift(managedShift);
               }

               return entity;
            })
            .collect(Collectors.toList());

      List<OperatorAssignmentEntity> savedEntities = repository.saveAll(entities);

      return savedEntities.stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
   }

   @Override
   public Optional<OperatorAssignment> findById(Long id) {
      return repository.findById(id)
            .map(mapper::toDomain);
   }

   @Override
   public List<OperatorAssignment> findByZoneId(Long zoneId) {
      return repository.findByZoneId(zoneId).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
   }

   @Override
   public List<OperatorAssignment> findActiveByZoneId(Long zoneId) {
      LocalDate today = LocalDate.now();
      return repository.findActiveByZoneId(zoneId, today).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
   }

   @Override
   public List<OperatorAssignment> findByOperatorId(Long operatorId) {
      return repository.findByOperatorId(operatorId).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
   }

   @Override
   public List<OperatorAssignment> findByShiftId(Long shiftId) {
      return repository.findByShiftId(shiftId).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
   }

   @Override
   public boolean existsActiveAssignment(Long operatorId, Long zoneId, Long shiftId) {
      LocalDate today = LocalDate.now();
      return repository.existsActiveAssignment(operatorId, zoneId, shiftId, today);
   }

   @Override
   public void deleteById(Long id) {
      repository.deleteById(id);
   }
}