package com.winnersystems.smartparking.parking.application.dto.query;

import java.time.LocalDateTime;

/**
 * DTO de consulta para Customer.
 *
 * @author Edwin Yoner - Winner Systems
 */
public record CustomerDto(
      Long id,
      Long documentTypeId,
      String documentTypeName,
      String documentNumber,
      String firstName,
      String lastName,
      String fullName,
      String phone,
      String email,
      String address,
      LocalDateTime registrationDate,
      LocalDateTime firstSeenDate,
      LocalDateTime lastSeenDate,
      Integer totalVisits,
      Boolean isRecurrent,
      Long authExternalId,
      Boolean hasMobileAccount,
      LocalDateTime createdAt,
      LocalDateTime updatedAt
) {}