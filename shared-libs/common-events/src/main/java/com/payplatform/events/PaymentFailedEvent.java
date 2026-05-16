package com.payplatform.events;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentFailedEvent(
        String paymentId,
        String orderId,
        String userId,
        String merchantId,
        BigDecimal amount,
        String currency,
        Instant failedAt,
        String failureCode,
        String failureReason,
        String correlationId
) {
}
