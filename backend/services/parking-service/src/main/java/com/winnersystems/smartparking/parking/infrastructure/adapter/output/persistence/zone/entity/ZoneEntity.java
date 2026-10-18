package com.winnersystems.smartparking.parking.infrastructure.adapter.output.persistence.zone.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad JPA para Zone. Configurada para Oracle Database.
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
@Entity
@Table(name = "ZONES")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZoneEntity {

   @Id
   @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zone_seq")
   @SequenceGenerator(name = "zone_seq", sequenceName = "SEQ_ZONE", allocationSize = 1)
   @Column(name = "ID")
   private Long id;

   @Column(name = "PARKING_ID", nullable = false)          // ← AGREGADO: FK a PARKINGS
   private Long parkingId;

   @Column(name = "NAME", nullable = false, length = 100)
   private String name;

   @Column(name = "CODE", nullable = false, unique = true, length = 50)
   private String code;

   @Column(name = "ADDRESS", nullable = false, length = 200)
   private String address;

   @Column(name = "DESCRIPTION", columnDefinition = "CLOB")
   private String description;

   @Column(name = "TOTAL_SPACES")
   private Integer totalSpaces;

   @Column(name = "AVAILABLE_SPACES")
   private Integer availableSpaces;

   @Column(name = "LATITUDE")
   private Double latitude;

   @Column(name = "LONGITUDE")
   private Double longitude;

   @Column(name = "HAS_CAMERA")
   private Boolean hasCamera;

   @Column(name = "CAMERA_IDS", length = 500)
   private String cameraIds;

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
      if (this.createdAt == null)       this.createdAt = LocalDateTime.now();
      if (this.updatedAt == null)       this.updatedAt = LocalDateTime.now();
      if (this.totalSpaces == null)     this.totalSpaces = 0;
      if (this.availableSpaces == null) this.availableSpaces = 0;
      if (this.hasCamera == null)       this.hasCamera = false;
   }

   @PreUpdate
   protected void onUpdate() {
      if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
   }
}