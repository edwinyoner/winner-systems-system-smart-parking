package com.winnersystems.smartparking.parking.infrastructure.adapter.output.persistence.space.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad JPA para Space. Configurada para Oracle Database.
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
@Entity
@Table(name = "SPACES")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpaceEntity {

   @Id
   @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "space_seq")
   @SequenceGenerator(name = "space_seq", sequenceName = "SEQ_SPACE", allocationSize = 1)
   @Column(name = "ID")
   private Long id;

   @Column(name = "ZONE_ID", nullable = false)
   private Long zoneId;

   @Column(name = "TYPE", nullable = false, length = 20)
   private String type;

   @Column(name = "CODE", nullable = false, unique = true, length = 20)
   private String code;

   @Column(name = "DESCRIPTION", length = 255)
   private String description;

   @Column(name = "WIDTH")
   private Double width;

   @Column(name = "LENGTH")
   private Double length;

   @Column(name = "HAS_SENSOR", nullable = false)
   private Boolean hasSensor;

   @Column(name = "SENSOR_ID", length = 50)
   private String sensorId;

   @Column(name = "HAS_CAMERA_COVERAGE", nullable = false)
   private Boolean hasCameraCoverage;

   @Column(name = "STATUS", nullable = false, length = 20)
   private String status;

   // ========================= AUDITORÍA =========================

   @Column(name = "CREATED_AT", nullable = false, updatable = false)
   private LocalDateTime createdAt;

   @Column(name = "CREATED_BY")
   private Long createdBy;

   @Column(name = "UPDATED_AT", nullable = false)
   private LocalDateTime updatedAt;

   @Column(name = "UPDATED_BY")
   private Long updatedBy;

   @Column(name = "DELETED_AT")
   private LocalDateTime deletedAt;

   @Column(name = "DELETED_BY")
   private Long deletedBy;

   // ========================= LIFECYCLE CALLBACKS =========================

   @PrePersist
   protected void onCreate() {
      if (this.createdAt == null)        this.createdAt = LocalDateTime.now();
      if (this.updatedAt == null)        this.updatedAt = LocalDateTime.now();
      if (this.hasSensor == null)        this.hasSensor = false;
      if (this.hasCameraCoverage == null) this.hasCameraCoverage = false;
   }

   @PreUpdate
   protected void onUpdate() {
      if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
   }
}