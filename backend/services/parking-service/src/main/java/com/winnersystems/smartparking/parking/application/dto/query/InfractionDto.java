package com.winnersystems.smartparking.parking.application.dto.query;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de consulta para Infraction.
 *
 * @author Edwin Yoner - Winner Systems
 */
public record InfractionDto(
      Long id,
      String infractionCode,
      Long parkingId,
      String parkingName,
      Long zoneId,
      String zoneName,
      Long spaceId,
      String spaceCode,
      Long transactionId,
      Long vehicleId,
      String vehiclePlate,
      Long customerId,
      String customerName,
      String infractionType,
      String severity,
      String description,
      String evidence,
      LocalDateTime detectedAt,
      Long detectedBy,
      String detectedByName,
      String detectionMethod,
      BigDecimal fineAmount,
      String currency,
      LocalDateTime fineDueDate,
      Boolean finePaid,
      LocalDateTime finePaidAt,
      String status,
      LocalDateTime resolvedAt,
      String resolutionType,
      String resolution,
      Boolean notificationSent,
      LocalDateTime createdAt
) {}