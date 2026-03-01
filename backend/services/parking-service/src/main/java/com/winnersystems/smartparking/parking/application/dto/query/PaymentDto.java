package com.winnersystems.smartparking.parking.application.dto.query;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de consulta para Payment.
 *
 * @author Edwin Yoner - Winner Systems
 */
public record PaymentDto(
      Long id,
      Long transactionId,
      Long paymentTypeId,
      String paymentTypeName,
      BigDecimal amount,
      String currency,
      LocalDateTime paymentDate,
      String referenceNumber,
      Long operatorId,
      String operatorName,
      String status,
      BigDecimal refundAmount,
      LocalDateTime refundDate,
      String refundReason,
      BigDecimal netAmount,
      LocalDateTime createdAt
) {}