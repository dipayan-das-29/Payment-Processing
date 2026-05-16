package com.payplatform.events;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentCompletedEvent(
        String paymentId,
        String orderId,
        String userId,
        String merchantId,
        BigDecimal amount,
        String currency,
        Instant completedAt,
        String bankReference,
        String correlationId
) {
}
