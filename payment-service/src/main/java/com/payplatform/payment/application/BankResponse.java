package com.payplatform.payment.application;

public record BankResponse(
        boolean approved,
        String reference,
        String failureCode,
        String failureReason
) {
}
