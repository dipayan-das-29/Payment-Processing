package com.payplatform.events;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FraudAlertEvent(
        String paymentId,
        String orderId,
        String userId,
        BigDecimal amount,
        String currency,
        String riskLevel,
        Integer riskScore,
        String reason,
        Instant detectedAt,
        String correlationId
) {
}
