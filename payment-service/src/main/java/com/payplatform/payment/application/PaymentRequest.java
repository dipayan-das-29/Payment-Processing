package com.payplatform.payment.application;

import java.math.BigDecimal;

public record PaymentRequest(
        String userId,
        String merchantId,
        String orderId,
        BigDecimal amount,
        String currency,
        String paymentMethod,
        String idempotencyKey,
        String correlationId
) {
}
