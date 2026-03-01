package com.winnersystems.smartparking.parking.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad de dominio que representa un VEHÍCULO.
 *
 * Un vehículo se identifica únicamente por su PLACA (licensePlate), que es
 * el identificador legal oficial. El sistema registra vehículos conforme
 * ingresan al estacionamiento, sin requerir registro previo.
 *
 * Información adicional como marca y color son opcionales y sirven como
 * ayuda visual para operadores. La fuente oficial de datos vehiculares es
 * SUNARP (a integrarse en fases posteriores).
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public class Vehicle {

   // ========================= CAMPOS DE IDENTIDAD =========================

   private Long id;
   private String licensePlate;                           // Placa única (ej: "ABC-123")

   // ========================= CAMPOS OPCIONALES (Ayuda Visual) =========================

   private String color;                                  // Color del vehículo (opcional)
   private String brand;                                  // Marca (opcional)

   // ========================= CAMPOS DE TRACKING =========================

   private LocalDateTime firstSeenDate;                   // Primera vez en el sistema
   private LocalDateTime lastSeenDate;                    // Última vez vista
   private Integer totalVisits;                           // Contador de visitas acumuladas

   // ========================= CAMPOS DE AUDITORÍA =========================

   private LocalDateTime createdAt;
   private Long createdBy;                                // ID del usuario que creó
   private LocalDateTime updatedAt;
   private Long updatedBy;                                // ID del usuario que actualizó
   private LocalDateTime deletedAt;                       // Soft delete
   private Long deletedBy;

   // ========================= CONSTRUCTORES =========================

   /**
    * Constructor vacío - Inicializa con valores por defecto.
    * El vehículo se crea con 0 visitas.
    */
   public Vehicle() {
      this.totalVisits = 0;
      this.createdAt = LocalDateTime.now();
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Constructor con placa.
    *
    * @param licensePlate placa del vehículo (única)
    */
   public Vehicle(String licensePlate) {
      this();
      this.licensePlate = licensePlate != null ? licensePlate.toUpperCase().trim() : null;
      this.firstSeenDate = LocalDateTime.now();
      this.lastSeenDate = LocalDateTime.now();
   }

   /**
    * Constructor completo.
    *
    * @param licensePlate placa del vehículo
    * @param color color del vehículo
    * @param brand marca del vehículo
    */
   public Vehicle(String licensePlate, String color, String brand) {
      this(licensePlate);
      this.color = color;
      this.brand = brand;
   }

   // ========================= MÉTODOS DE NEGOCIO - INFORMACIÓN =========================

   // OPCIONAL: Mejorar validación de placa peruana
   public boolean hasValidPeruvianPlate() {
      if (licensePlate == null) return false;

      // Formatos Perú:
      // Antiguo: ABC-123 (3 letras + 3 números)
      // Nuevo: A1B-234 (letra-número-letra + 3 números)
      String cleaned = licensePlate.replaceAll("[^A-Z0-9]", "");

      if (cleaned.length() != 6) return false;

      // Antiguo: AAA999
      boolean oldFormat = cleaned.substring(0, 3).matches("[A-Z]{3}") &&
            cleaned.substring(3).matches("[0-9]{3}");

      // Nuevo: A9A999
      boolean newFormat = cleaned.matches("[A-Z][0-9][A-Z][0-9]{3}");

      return oldFormat || newFormat;
   }

   /**
    * Obtiene un identificador legible del vehículo.
    *
    * @return descripción formateada del vehículo
    */
   public String getDisplayName() {
      StringBuilder sb = new StringBuilder();
      sb.append(licensePlate);

      if (brand != null && !brand.isEmpty()) {
         sb.append(" (").append(brand);
         if (color != null && !color.isEmpty()) {
            sb.append(" ").append(color);
         }
         sb.append(")");
      } else if (color != null && !color.isEmpty()) {
         sb.append(" (").append(color).append(")");
      }

      return sb.toString();
   }

   /**
    * Verifica si el vehículo es recurrente (ha visitado más de una vez).
    *
    * @return true si totalVisits > 1
    */
   public boolean isRecurrent() {
      return totalVisits != null && totalVisits > 1;
   }

   /**
    * Verifica si el vehículo es nuevo (primera visita).
    *
    * @return true si totalVisits <= 1
    */
   public boolean isNew() {
      return totalVisits == null || totalVisits <= 1;
   }

   // ========================= MÉTODOS DE NEGOCIO - TRACKING =========================

   /**
    * Registra una nueva visita del vehículo.
    * Incrementa el contador y actualiza la última vez vista.
    */
   public void recordVisit() {
      if (this.totalVisits == null) {
         this.totalVisits = 0;
      }
      this.totalVisits++;
      this.lastSeenDate = LocalDateTime.now();
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Actualiza la última vez que el vehículo fue visto.
    * Útil para actualizar sin incrementar visitas.
    */
   public void updateLastSeen() {
      this.lastSeenDate = LocalDateTime.now();
      this.updatedAt = LocalDateTime.now();
   }

   // ========================= MÉTODOS DE NEGOCIO - SOFT DELETE =========================

   /**
    * Marca el vehículo como eliminado (soft delete).
    * Uso: vehículos registrados por error.
    *
    * @param deletedByUserId ID del usuario que elimina
    */
   public void markAsDeleted(Long deletedByUserId) {
      this.deletedAt = LocalDateTime.now();
      this.deletedBy = deletedByUserId;
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Restaura un vehículo previamente eliminado.
    */
   public void restore() {
      this.deletedAt = null;
      this.deletedBy = null;
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Verifica si el vehículo está eliminado.
    *
    * @return true si deletedAt no es null
    */
   public boolean isDeleted() {
      return deletedAt != null;
   }

   // ========================= MÉTODOS DE NEGOCIO - ACTUALIZACIÓN DE DATOS =========================

   /**
    * Actualiza la información visual del vehículo.
    *
    * @param color nuevo color
    * @param brand nueva marca
    */
   public void updateVisualInfo(String color, String brand) {
      this.color = color;
      this.brand = brand;
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Actualiza solo el color.
    *
    * @param color nuevo color
    */
   public void updateColor(String color) {
      this.color = color;
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Actualiza solo la marca.
    *
    * @param brand nueva marca
    */
   public void updateBrand(String brand) {
      this.brand = brand;
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Normaliza la placa a formato estándar (mayúsculas, sin espacios).
    */
   public void normalizeLicensePlate() {
      if (this.licensePlate != null) {
         this.licensePlate = this.licensePlate.toUpperCase().trim().replaceAll("\\s+", "");
         this.updatedAt = LocalDateTime.now();
      }
   }

   /**
    * Actualiza el usuario que modificó este registro.
    *
    * @param userId ID del usuario que modifica
    */
   public void updateModifiedBy(Long userId) {
      this.updatedBy = userId;
      this.updatedAt = LocalDateTime.now();
   }

   // ========================= MÉTODOS DE VALIDACIÓN =========================

   /**
    * Verifica si el vehículo tiene información visual completa.
    *
    * @return true si tiene color Y marca
    */
   public boolean hasCompleteVisualInfo() {
      return color != null && !color.isEmpty() && brand != null && !brand.isEmpty();
   }

   /**
    * Verifica si tiene color registrado.
    *
    * @return true si color no es null ni vacío
    */
   public boolean hasColor() {
      return color != null && !color.isEmpty();
   }

   /**
    * Verifica si tiene marca registrada.
    *
    * @return true si brand no es null ni vacío
    */
   public boolean hasBrand() {
      return brand != null && !brand.isEmpty();
   }

   /**
    * Verifica si la placa tiene formato válido básico.
    * Formato Perú: 3 letras + 3 números (ej: ABC-123) o variantes.
    *
    * @return true si tiene formato aproximadamente válido
    */
   public boolean hasValidLicensePlateFormat() {
      if (licensePlate == null || licensePlate.isEmpty()) {
         return false;
      }
      // Formato básico: al menos 5 caracteres alfanuméricos
      String cleaned = licensePlate.replaceAll("[^A-Z0-9]", "");
      return cleaned.length() >= 5 && cleaned.length() <= 8;
   }

   // ========================= GETTERS Y SETTERS =========================

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public String getLicensePlate() {
      return licensePlate;
   }

   public void setLicensePlate(String licensePlate) {
      this.licensePlate = licensePlate != null ? licensePlate.toUpperCase().trim() : null;
   }

   public String getColor() {
      return color;
   }

   public void setColor(String color) {
      this.color = color;
   }

   public String getBrand() {
      return brand;
   }

   public void setBrand(String brand) {
      this.brand = brand;
   }

   public LocalDateTime getFirstSeenDate() {
      return firstSeenDate;
   }

   public void setFirstSeenDate(LocalDateTime firstSeenDate) {
      this.firstSeenDate = firstSeenDate;
   }

   public LocalDateTime getLastSeenDate() {
      return lastSeenDate;
   }

   public void setLastSeenDate(LocalDateTime lastSeenDate) {
      this.lastSeenDate = lastSeenDate;
   }

   public Integer getTotalVisits() {
      return totalVisits;
   }

   public void setTotalVisits(Integer totalVisits) {
      this.totalVisits = totalVisits;
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

   public LocalDateTime getUpdatedAt() {
      return updatedAt;
   }

   public void setUpdatedAt(LocalDateTime updatedAt) {
      this.updatedAt = updatedAt;
   }

   public Long getUpdatedBy() {
      return updatedBy;
   }

   public void setUpdatedBy(Long updatedBy) {
      this.updatedBy = updatedBy;
   }

   public LocalDateTime getDeletedAt() {
      return deletedAt;
   }

   public void setDeletedAt(LocalDateTime deletedAt) {
      this.deletedAt = deletedAt;
   }

   public Long getDeletedBy() {
      return deletedBy;
   }

   public void setDeletedBy(Long deletedBy) {
      this.deletedBy = deletedBy;
   }

   // ========================= EQUALS, HASHCODE Y TOSTRING =========================

   /**
    * Dos vehículos son iguales si tienen el mismo ID o la misma placa.
    */
   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Vehicle vehicle = (Vehicle) o;
      return Objects.equals(id, vehicle.id) ||
            Objects.equals(licensePlate, vehicle.licensePlate);
   }

   /**
    * HashCode basado en ID y placa únicamente.
    */
   @Override
   public int hashCode() {
      return Objects.hash(id, licensePlate);
   }

   /**
    * ToString para logging y debugging.
    */
   @Override
   public String toString() {
      return "Vehicle{" +
            "id=" + id +
            ", licensePlate='" + licensePlate + '\'' +
            ", color='" + color + '\'' +
            ", brand='" + brand + '\'' +
            ", totalVisits=" + totalVisits +
            ", isRecurrent=" + isRecurrent() +
            '}';
   }
}