package com.payplatform.fraud.infrastructure;

import com.payplatform.events.FraudAlertEvent;
import com.payplatform.events.PaymentInitiatedEvent;
import com.payplatform.fraud.application.FraudAssessmentResponse;
import com.payplatform.fraud.application.FraudRuleEngine;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.stream.Collectors;

@Component
public class FraudEventConsumer {

    private final FraudRuleEngine fraudRuleEngine;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public FraudEventConsumer(FraudRuleEngine fraudRuleEngine, KafkaTemplate<String, Object> kafkaTemplate) {
        this.fraudRuleEngine = fraudRuleEngine;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "payment.initiated", groupId = "fraud-detection-service")
    public void onPaymentInitiated(PaymentInitiatedEvent event) {
        FraudAssessmentResponse assessment = fraudRuleEngine.evaluate(event);
        if (assessment.riskLevel().name().equals("HIGH_RISK")) {
            kafkaTemplate.send("fraud.alert", event.paymentId(), new FraudAlertEvent(
                    event.paymentId(),
                    event.orderId(),
                    event.userId(),
                    event.amount(),
                    event.currency(),
                    assessment.riskLevel().name(),
                    assessment.score(),
                    assessment.signals().stream()
                            .filter(s -> s.triggered())
                            .map(s -> s.code())
                            .collect(Collectors.joining(",")),
                    Instant.now(),
                    event.correlationId()
            ));
        }
    }
}
