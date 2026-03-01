package com.winnersystems.smartparking.parking.application.dto.query;

import java.time.LocalDateTime;

/**
 * DTO de consulta para Vehicle.
 *
 * @author Edwin Yoner - Winner Systems
 */
public record VehicleDto(
      Long id,
      String licensePlate,
      String color,
      String brand,
      String displayName,
      LocalDateTime firstSeenDate,
      LocalDateTime lastSeenDate,
      Integer totalVisits,
      Boolean isRecurrent,
      LocalDateTime createdAt,
      LocalDateTime updatedAt
) {}