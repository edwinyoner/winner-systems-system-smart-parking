package com.winnersystems.smartparking.parking.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad de dominio que representa la ASIGNACIÓN de un OPERADOR a una ZONA.
 *
 * Registra qué operador trabaja en qué zona, en qué turno y durante qué período.
 * Es un registro HISTÓRICO que documenta asignaciones pasadas, presentes y futuras.
 *
 * Características principales:
 * - NO tiene soft delete (es un registro histórico inmutable)
 * - NO tiene campo status (se determina mediante endDate)
 * - NO almacena daysOfWeek (esa información está en Shift)
 *
 * Ciclo de vida:
 * 1. CREACIÓN: Se asigna operador a zona con startDate (y opcionalmente endDate)
 * 2. ACTIVA: endDate == null (indefinida) || endDate > hoy
 * 3. FINALIZADA: endDate != null && endDate <= hoy
 *
 * Casos de uso:
 * - Asignar operador a zona específica para un turno
 * - Consultar qué operadores trabajan en una zona
 * - Consultar en qué zonas trabaja un operador
 * - Histórico de asignaciones para auditoría
 *
 * Relaciones:
 * - operatorId → FK a User en auth-service (NO FK en BD, relación lógica)
 * - zoneId → FK a Zone
 * - shiftId → FK a Shift
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public class OperatorAssignment {

   // ========================= CAMPOS DE IDENTIDAD =========================

   private Long id;

   // ========================= RELACIONES (FKs) =========================

   private Long operatorId;                               // FK a User en auth-service (NOT NULL)
   private Long zoneId;                                   // FK a Zone (NOT NULL)
   private Long shiftId;                                  // FK a Shift (NOT NULL)

   // ========================= CAMPOS DE PERÍODO =========================

   private LocalDate startDate;                           // Fecha inicio asignación (NOT NULL)
   private LocalDate endDate;                             // Fecha fin (nullable = indefinido)

   // ========================= CAMPOS DE AUDITORÍA =========================

   private LocalDateTime createdAt;
   private Long createdBy;                                // ID del usuario que creó
   private LocalDateTime updatedAt;
   private Long updatedBy;                                // ID del usuario que actualizó

   // ========================= CONSTRUCTORES =========================

   /**
    * Constructor vacío - Inicializa con valores por defecto.
    * La asignación inicia hoy y es indefinida (sin endDate).
    */
   public OperatorAssignment() {
      this.startDate = LocalDate.now();
      this.createdAt = LocalDateTime.now();
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Constructor con campos básicos.
    * Crea una asignación indefinida (sin fecha de fin).
    *
    * @param operatorId ID del operador
    * @param zoneId ID de la zona
    * @param shiftId ID del turno
    */
   public OperatorAssignment(Long operatorId, Long zoneId, Long shiftId) {
      this();
      this.operatorId = operatorId;
      this.zoneId = zoneId;
      this.shiftId = shiftId;
   }

   /**
    * Constructor completo con período específico.
    *
    * @param operatorId ID del operador
    * @param zoneId ID de la zona
    * @param shiftId ID del turno
    * @param startDate fecha de inicio
    * @param endDate fecha de fin (puede ser null)
    */
   public OperatorAssignment(Long operatorId, Long zoneId, Long shiftId,
                             LocalDate startDate, LocalDate endDate) {
      this();
      this.operatorId = operatorId;
      this.zoneId = zoneId;
      this.shiftId = shiftId;
      this.startDate = startDate != null ? startDate : LocalDate.now();
      this.endDate = endDate;
   }

   // ========================= MÉTODOS DE NEGOCIO - CONSULTA DE ESTADO =========================

   /**
    * Verifica si la asignación está activa actualmente.
    * Una asignación está activa si:
    * - Ya comenzó (startDate <= hoy)
    * - Y no tiene fecha de fin O la fecha de fin es futura
    *
    * @return true si está activa, false si no
    */
   public boolean isActive() {
      LocalDate today = LocalDate.now();
      boolean hasStarted = startDate.isBefore(today) || startDate.isEqual(today);
      boolean hasNotEnded = endDate == null || endDate.isAfter(today);
      return hasStarted && hasNotEnded;
   }

   /**
    * Verifica si la asignación ha finalizado.
    *
    * @return true si ya terminó, false si sigue activa
    */
   public boolean hasEnded() {
      return endDate != null && endDate.isBefore(LocalDate.now());
   }

   /**
    * Verifica si la asignación es indefinida (sin fecha de fin).
    *
    * @return true si no tiene endDate, false si tiene
    */
   public boolean isIndefinite() {
      return endDate == null;
   }

   /**
    * Verifica si la asignación está programada para el futuro.
    *
    * @return true si aún no ha comenzado, false si ya comenzó
    */
   public boolean isPending() {
      return startDate.isAfter(LocalDate.now());
   }

   // ========================= MÉTODOS DE NEGOCIO - MODIFICACIÓN =========================

   /**
    * Finaliza la asignación inmediatamente.
    * Establece la fecha de fin como hoy.
    */
   public void endAssignment() {
      this.endDate = LocalDate.now();
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Finaliza la asignación en una fecha específica.
    *
    * @param endDate fecha de finalización
    * @throws IllegalArgumentException si endDate es anterior a startDate
    */
   public void endAssignmentOn(LocalDate endDate) {
      if (endDate.isBefore(this.startDate)) {
         throw new IllegalArgumentException(
               "La fecha de fin no puede ser anterior a la fecha de inicio");
      }
      this.endDate = endDate;
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Extiende la asignación a una nueva fecha de fin.
    *
    * @param newEndDate nueva fecha de fin
    * @throws IllegalArgumentException si la nueva fecha es anterior a startDate
    */
   public void extendUntil(LocalDate newEndDate) {
      if (newEndDate.isBefore(this.startDate)) {
         throw new IllegalArgumentException(
               "La fecha de fin no puede ser anterior a la fecha de inicio");
      }
      this.endDate = newEndDate;
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Hace la asignación indefinida (elimina fecha de fin).
    */
   public void makeIndefinite() {
      this.endDate = null;
      this.updatedAt = LocalDateTime.now();
   }

   // ========================= MÉTODOS DE VALIDACIÓN =========================

   /**
    * Valida que la asignación tenga todos los datos requeridos.
    *
    * @return true si la asignación es válida, false si no
    */
   public boolean isValid() {
      return operatorId != null
            && zoneId != null
            && shiftId != null
            && startDate != null;
   }

   /**
    * Valida las fechas de la asignación.
    *
    * @return true si las fechas son válidas, false si no
    */
   public boolean hasValidDates() {
      if (startDate == null) {
         return false;
      }
      if (endDate != null && endDate.isBefore(startDate)) {
         return false;
      }
      return true;
   }

   // ========================= MÉTODOS DE INFORMACIÓN =========================

   /**
    * Calcula la duración total de la asignación en días.
    * Si la asignación es indefinida, calcula desde startDate hasta hoy.
    *
    * @return duración en días
    */
   public long getDurationInDays() {
      LocalDate end = endDate != null ? endDate : LocalDate.now();
      return java.time.temporal.ChronoUnit.DAYS.between(startDate, end);
   }

   /**
    * Obtiene el estado descriptivo de la asignación.
    *
    * @return "ACTIVA", "FINALIZADA" o "PENDIENTE"
    */
   public String getStatus() {
      if (isPending()) {
         return "PENDIENTE";
      }
      if (isActive()) {
         return "ACTIVA";
      }
      return "FINALIZADA";
   }

   // ========================= GETTERS Y SETTERS =========================

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public Long getOperatorId() {
      return operatorId;
   }

   public void setOperatorId(Long operatorId) {
      this.operatorId = operatorId;
   }

   public Long getZoneId() {
      return zoneId;
   }

   public void setZoneId(Long zoneId) {
      this.zoneId = zoneId;
   }

   public Long getShiftId() {
      return shiftId;
   }

   public void setShiftId(Long shiftId) {
      this.shiftId = shiftId;
   }

   public LocalDate getStartDate() {
      return startDate;
   }

   public void setStartDate(LocalDate startDate) {
      this.startDate = startDate;
   }

   public LocalDate getEndDate() {
      return endDate;
   }

   public void setEndDate(LocalDate endDate) {
      this.endDate = endDate;
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

   // ========================= EQUALS, HASHCODE Y TOSTRING =========================

   /**
    * Dos asignaciones son iguales si tienen el mismo ID.
    */
   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      OperatorAssignment that = (OperatorAssignment) o;
      return Objects.equals(id, that.id);
   }

   /**
    * HashCode basado únicamente en el ID.
    */
   @Override
   public int hashCode() {
      return Objects.hash(id);
   }

   /**
    * ToString para logging y debugging.
    */
   @Override
   public String toString() {
      return "OperatorAssignment{" +
            "id=" + id +
            ", operatorId=" + operatorId +
            ", zoneId=" + zoneId +
            ", shiftId=" + shiftId +
            ", startDate=" + startDate +
            ", endDate=" + endDate +
            ", status='" + getStatus() + '\'' +
            ", durationDays=" + getDurationInDays() +
            '}';
   }
}