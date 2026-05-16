package com.payplatform.payment.application;

import com.payplatform.payment.domain.Payment;
import com.payplatform.payment.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        String paymentId,
        String orderId,
        String userId,
        String merchantId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String bankReference,
        String failureCode,
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getMerchantId(),
                payment.getAmount().getAmount(),
                payment.getAmount().getCurrency(),
                payment.getStatus(),
                payment.getBankReference(),
                payment.getFailureCode(),
                payment.getFailureReason(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
