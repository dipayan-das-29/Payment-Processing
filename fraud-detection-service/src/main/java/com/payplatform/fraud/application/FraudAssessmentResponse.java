package com.payplatform.fraud.application;

import com.payplatform.fraud.domain.FraudRiskLevel;
import com.payplatform.fraud.domain.FraudSignal;

import java.util.List;

public record FraudAssessmentResponse(
        String paymentId,
        int score,
        FraudRiskLevel riskLevel,
        List<FraudSignal> signals
) {
}
