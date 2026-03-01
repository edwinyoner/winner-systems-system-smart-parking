package com.winnersystems.smartparking.parking.infrastructure.adapter.input.rest.operatorassignment.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response con información simplificada de una asignación de operador.
 * Usado en listados y respuestas rápidas.
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public record OperatorAssignmentResponse(
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