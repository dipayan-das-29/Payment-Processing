package com.payplatform.fraud.domain;

public record FraudSignal(
        String code,
        String description,
        int weight,
        boolean triggered
) {
}
