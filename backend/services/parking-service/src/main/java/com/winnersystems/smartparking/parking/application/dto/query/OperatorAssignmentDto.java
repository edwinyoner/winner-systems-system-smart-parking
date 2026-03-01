package com.winnersystems.smartparking.parking.application.dto.query;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO simplificado para información básica de asignación de operador.
 * Usado para listados y referencias rápidas.
 *
 * @param id identificador de la asignación
 * @param operatorId ID del operador asignado
 * @param operatorName nombre completo del operador
 * @param zoneId ID de la zona
 * @param zoneName nombre de la zona
 * @param shiftId ID del turno
 * @param shiftName nombre del turno
 * @param startDate fecha de inicio de la asignación
 * @param endDate fecha de fin de la asignación (null = indefinida)
 * @param status estado calculado: "ACTIVA", "FINALIZADA", "PENDIENTE"
 * @param createdAt fecha de creación del registro
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public record OperatorAssignmentDto(
      Long id,
      Long operatorId,
      String operatorName,
      Long zoneId,
      String zoneName,
      Long shiftId,
      String shiftName,
      LocalDate startDate,
      LocalDate endDate,
      String status,
      LocalDateTime createdAt
) {}