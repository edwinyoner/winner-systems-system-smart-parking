package com.winnersystems.smartparking.parking.infrastructure.adapter.output.persistence.operatorassignment.entity;

import com.winnersystems.smartparking.parking.infrastructure.adapter.output.persistence.shift.entity.ShiftEntity;
import com.winnersystems.smartparking.parking.infrastructure.adapter.output.persistence.zone.entity.ZoneEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad JPA para OPERATOR_ASSIGNMENT. Configurada para Oracle Database.
 *
 * Representa la asignación de un operador a una zona específica en un turno.
 * No tiene soft delete porque es un registro histórico.
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
@Entity
@Table(name = "OPERATOR_ASSIGNMENTS")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperatorAssignmentEntity {

   @Id
   @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "operator_assignment_seq")
   @SequenceGenerator(name = "operator_assignment_seq", sequenceName = "SEQ_OPERATOR_ASSIGNMENT", allocationSize = 1)
   @Column(name = "ID")
   private Long id;

   // ========================= RELACIONES (FKs) =========================

   @Column(name = "OPERATOR_ID", nullable = false)
   private Long operatorId;  // FK lógica a User en auth-service (NO FK en BD)

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "ZONE_ID", nullable = false)
   private ZoneEntity zone;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "SHIFT_ID", nullable = false)
   private ShiftEntity shift;

   // ========================= CAMPOS DE PERÍODO =========================

   @Column(name = "START_DATE", nullable = false)
   private LocalDate startDate;

   @Column(name = "END_DATE")
   private LocalDate endDate;

   // ========================= CAMPOS DE AUDITORÍA =========================

   @CreatedDate
   @Column(name = "CREATED_AT", nullable = false, updatable = false)
   private LocalDateTime createdAt;

   @CreatedBy
   @Column(name = "CREATED_BY")
   private Long createdBy;

   @LastModifiedDate
   @Column(name = "UPDATED_AT", nullable = false)
   private LocalDateTime updatedAt;

   @LastModifiedBy
   @Column(name = "UPDATED_BY")
   private Long updatedBy;

   // ========================= LIFECYCLE CALLBACKS =========================

   @PrePersist
   protected void onCreate() {
      if (this.createdAt == null) this.createdAt = LocalDateTime.now();
      if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
      if (this.startDate == null) this.startDate = LocalDate.now();
   }

   @PreUpdate
   protected void onUpdate() {
      if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
   }
}