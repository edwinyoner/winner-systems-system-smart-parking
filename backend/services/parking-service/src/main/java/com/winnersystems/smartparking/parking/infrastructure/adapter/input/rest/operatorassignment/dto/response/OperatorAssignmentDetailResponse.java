package com.winnersystems.smartparking.parking.infrastructure.adapter.input.rest.operatorassignment.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response con información completa de una asignación de operador.
 * Incluye detalles del operador, zona y turno.
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public record OperatorAssignmentDetailResponse(
      Long id,
      LocalDate startDate,
      LocalDate endDate,
      String status,
      Long durationDays,
      OperatorInfo operator,
      ZoneInfo zone,
      ShiftInfo shift,
      LocalDateTime createdAt,
      Long createdBy,
      LocalDateTime updatedAt,
      Long updatedBy
) {

   /**
    * Información del operador asignado.
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
    */
   public record ZoneInfo(
         Long id,
         String name,
         String code
   ) {}

   /**
    * Información del turno asignado.
    */
   public record ShiftInfo(
         Long id,
         String name,
         String startTime,
         String endTime
   ) {}
}