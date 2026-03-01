package com.winnersystems.smartparking.parking.application.dto.query;

import java.time.LocalDateTime;

/**
 * DTO de consulta para CustomerVehicle.
 *
 * @author Edwin Yoner - Winner Systems
 */
public record CustomerVehicleDto(
      Long id,
      Long customerId,
      String customerName,
      String customerDocument,
      Long vehicleId,
      String vehiclePlate,
      String vehicleBrand,
      String vehicleColor,
      LocalDateTime createdAt,
      Long createdBy
) {}