package com.winnersystems.smartparking.parking.application.dto.query;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO completo con todos los detalles de una asignación de operador.
 * Usado para consultas individuales donde se necesita toda la información.
 *
 * Incluye información completa del operador, zona y turno mediante inner records.
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public record OperatorAssignmentDetailDto(

      // ========================= BÁSICO =========================
      Long id,
      LocalDate startDate,
      LocalDate endDate,
      String status,
      Long durationDays,

      // ========================= ENTIDADES ANIDADAS =========================
      OperatorInfo operator,
      ZoneInfo zone,
      ShiftInfo shift,

      // ========================= AUDITORÍA =========================
      LocalDateTime createdAt,
      Long createdBy,
      LocalDateTime updatedAt,
      Long updatedBy

) {

   // ========================= RECORDS INTERNOS =========================

   /**
    * Información del operador asignado.
    *
    * @param id ID del operador en auth-service
    * @param firstName nombre del operador
    * @param lastName apellido del operador
    * @param fullName nombre completo
    * @param email email del operador
    * @param phoneNumber teléfono del operador
    */
   public record OperatorInfo(
         Long id,
         String firstName,
         String lastName,
         String fullName,
         String email,
         String phoneNumber
   ) {}

   /**
    * Información de la zona asignada.
    *
    * @param id ID de la zona
    * @param name nombre de la zona
    * @param code código de la zona
    */
   public record ZoneInfo(
         Long id,
         String name,
         String code
   ) {}

   /**
    * Información del turno asignado.
    *
    * @param id ID del turno
    * @param name nombre del turno
    * @param startTime hora de inicio
    * @param endTime hora de fin
    */
   public record ShiftInfo(
         Long id,
         String name,
         String startTime,
         String endTime
   ) {}
}