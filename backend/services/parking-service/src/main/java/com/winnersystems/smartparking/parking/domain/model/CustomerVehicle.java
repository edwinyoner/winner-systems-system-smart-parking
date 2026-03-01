package com.winnersystems.smartparking.parking.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad de dominio que representa el REGISTRO de uso entre un CLIENTE y un VEHÍCULO.
 *
 * Esta tabla registra qué clientes han usado qué vehículos en el sistema.
 * Se crea un registro la primera vez que un cliente usa un vehículo específico.
 * Los registros NO se eliminan, sirven como histórico de uso.
 *
 * Relación muchos a muchos (M:N):
 * - Un cliente puede haber usado varios vehículos
 * - Un vehículo puede haber sido usado por varios clientes
 *
 * Uso operativo:
 * - Sugerencia al operador: "Este auto normalmente lo usa Juan Pérez"
 * - Pre-carga de datos frecuentes
 * - Histórico de relaciones cliente-vehículo
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public class CustomerVehicle {

   // ========================= CAMPOS DE IDENTIDAD =========================

   private Long id;
   private Long customerId;                               // FK a Customer (NOT NULL)
   private Long vehicleId;                                // FK a Vehicle (NOT NULL)

   // ========================= CAMPOS DE REGISTRO =========================

   private LocalDateTime createdAt;                       // Cuándo se registró por primera vez
   private Long createdBy;                                // ID del operador que registró

   // ========================= CONSTRUCTORES =========================

   /**
    * Constructor vacío - Inicializa con valores por defecto.
    */
   public CustomerVehicle() {
      this.createdAt = LocalDateTime.now();
   }

   /**
    * Constructor con campos básicos.
    *
    * @param customerId ID del cliente
    * @param vehicleId ID del vehículo
    */
   public CustomerVehicle(Long customerId, Long vehicleId) {
      this();
      this.customerId = customerId;
      this.vehicleId = vehicleId;
   }

   // ========================= MÉTODOS DE VALIDACIÓN =========================

   /**
    * Verifica si el registro es válido (tiene customer y vehicle).
    *
    * @return true si tiene ambos IDs
    */
   public boolean isValid() {
      return customerId != null && vehicleId != null;
   }

   // OPCIONAL: Contador de veces que usó este vehículo
   private Integer usageCount;  // Cuántas veces este cliente usó este vehículo

   public void incrementUsage() {
      if (this.usageCount == null) {
         this.usageCount = 0;
      }
      this.usageCount++;
   }

   public boolean isFrequentCombination() {
      return usageCount != null && usageCount > 5;
   }

   // ========================= GETTERS Y SETTERS =========================

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public Long getCustomerId() {
      return customerId;
   }

   public void setCustomerId(Long customerId) {
      this.customerId = customerId;
   }

   public Long getVehicleId() {
      return vehicleId;
   }

   public void setVehicleId(Long vehicleId) {
      this.vehicleId = vehicleId;
   }

   public LocalDateTime getCreatedAt() {
      return createdAt;
   }

   public void setCreatedAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
   }

   public Long getCreatedBy() {
      return createdBy;
   }

   public void setCreatedBy(Long createdBy) {
      this.createdBy = createdBy;
   }

   // ========================= EQUALS, HASHCODE Y TOSTRING =========================

   /**
    * Dos registros son iguales si tienen el mismo ID o mismo customer+vehicle.
    */
   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      CustomerVehicle that = (CustomerVehicle) o;
      return Objects.equals(id, that.id) ||
            (Objects.equals(customerId, that.customerId) && Objects.equals(vehicleId, that.vehicleId));
   }

   /**
    * HashCode basado en ID, customerId y vehicleId.
    */
   @Override
   public int hashCode() {
      return Objects.hash(id, customerId, vehicleId);
   }

   /**
    * ToString para logging y debugging.
    */
   @Override
   public String toString() {
      return "CustomerVehicle{" +
            "id=" + id +
            ", customerId=" + customerId +
            ", vehicleId=" + vehicleId +
            ", createdAt=" + createdAt +
            '}';
   }
}