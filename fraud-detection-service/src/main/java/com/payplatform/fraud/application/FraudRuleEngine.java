package com.payplatform.fraud.application;

import com.payplatform.events.PaymentInitiatedEvent;
import com.payplatform.fraud.domain.FraudRiskLevel;
import com.payplatform.fraud.domain.FraudSignal;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class FraudRuleEngine {
    private final StringRedisTemplate redisTemplate;

    public FraudRuleEngine(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public FraudAssessmentResponse evaluate(PaymentInitiatedEvent event) {
        List<FraudSignal> signals = new ArrayList<>();
        signals.add(velocityRule(event));
        signals.add(amountAnomalyRule(event));
        signals.add(blacklistRule(event));
        signals.add(geoRule(event));

        int score = signals.stream()
                .filter(FraudSignal::triggered)
                .mapToInt(FraudSignal::weight)
                .sum();

        FraudRiskLevel riskLevel = score > 70
                ? FraudRiskLevel.HIGH_RISK
                : score > 40 ? FraudRiskLevel.MEDIUM_RISK : FraudRiskLevel.LOW_RISK;

        return new FraudAssessmentResponse(event.paymentId(), score, riskLevel, signals);
    }

    private FraudSignal velocityRule(PaymentInitiatedEvent event) {
        String key = "fraud:velocity:" + event.userId();
        Long count = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofSeconds(60));
        boolean triggered = count != null && count > 5;
        return new FraudSignal("VELOCITY", "More than 5 payments in 60 seconds", 35, triggered);
    }

    private FraudSignal amountAnomalyRule(PaymentInitiatedEvent event) {
        boolean triggered = event.amount().compareTo(new BigDecimal("50000")) > 0;
        return new FraudSignal("AMOUNT_ANOMALY", "Amount exceeds heuristic threshold", 30, triggered);
    }

    private FraudSignal blacklistRule(PaymentInitiatedEvent event) {
        boolean triggered = event.paymentMethod() != null && event.paymentMethod().startsWith("BLACKLISTED");
        return new FraudSignal("BIN_BLACKLIST", "Payment method matched blacklist prefix", 40, triggered);
    }

    private FraudSignal geoRule(PaymentInitiatedEvent event) {
        boolean triggered = false;
        return new FraudSignal("GEO_MISMATCH", "Country mismatch against user profile placeholder", 20, triggered);
    }
}
