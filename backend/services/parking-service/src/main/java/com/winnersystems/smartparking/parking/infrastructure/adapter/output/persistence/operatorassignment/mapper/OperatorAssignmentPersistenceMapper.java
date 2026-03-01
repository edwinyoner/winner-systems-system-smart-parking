package com.winnersystems.smartparking.parking.infrastructure.adapter.output.persistence.operatorassignment.mapper;

import com.winnersystems.smartparking.parking.domain.model.OperatorAssignment;
import com.winnersystems.smartparking.parking.infrastructure.adapter.output.persistence.operatorassignment.entity.OperatorAssignmentEntity;
import com.winnersystems.smartparking.parking.infrastructure.adapter.output.persistence.shift.entity.ShiftEntity;
import com.winnersystems.smartparking.parking.infrastructure.adapter.output.persistence.zone.entity.ZoneEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre OperatorAssignment (Domain) y OperatorAssignmentEntity (JPA).
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
@Component
public class OperatorAssignmentPersistenceMapper {

   /**
    * Convierte del modelo de dominio a entidad JPA.
    *
    * @param domain modelo de dominio
    * @return entidad JPA
    */
   public OperatorAssignmentEntity toEntity(OperatorAssignment domain) {
      if (domain == null) {
         return null;
      }

      OperatorAssignmentEntity entity = new OperatorAssignmentEntity();
      entity.setId(domain.getId());
      entity.setOperatorId(domain.getOperatorId());

      // Relaciones - solo setear IDs, las entidades se cargarán en el Adapter
      if (domain.getZoneId() != null) {
         ZoneEntity zone = new ZoneEntity();
         zone.setId(domain.getZoneId());
         entity.setZone(zone);
      }

      if (domain.getShiftId() != null) {
         ShiftEntity shift = new ShiftEntity();
         shift.setId(domain.getShiftId());
         entity.setShift(shift);
      }

      entity.setStartDate(domain.getStartDate());
      entity.setEndDate(domain.getEndDate());
      entity.setCreatedAt(domain.getCreatedAt());
      entity.setCreatedBy(domain.getCreatedBy());
      entity.setUpdatedAt(domain.getUpdatedAt());
      entity.setUpdatedBy(domain.getUpdatedBy());

      return entity;
   }

   /**
    * Convierte de entidad JPA a modelo de dominio.
    *
    * @param entity entidad JPA
    * @return modelo de dominio
    */
   public OperatorAssignment toDomain(OperatorAssignmentEntity entity) {
      if (entity == null) {
         return null;
      }

      OperatorAssignment domain = new OperatorAssignment();
      domain.setId(entity.getId());
      domain.setOperatorId(entity.getOperatorId());

      // Extraer IDs de las relaciones
      if (entity.getZone() != null) {
         domain.setZoneId(entity.getZone().getId());
      }

      if (entity.getShift() != null) {
         domain.setShiftId(entity.getShift().getId());
      }

      domain.setStartDate(entity.getStartDate());
      domain.setEndDate(entity.getEndDate());
      domain.setCreatedAt(entity.getCreatedAt());
      domain.setCreatedBy(entity.getCreatedBy());
      domain.setUpdatedAt(entity.getUpdatedAt());
      domain.setUpdatedBy(entity.getUpdatedBy());

      return domain;
   }
}