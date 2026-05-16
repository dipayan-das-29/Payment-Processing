package com.payplatform.payment.infrastructure;

import com.payplatform.events.EventTopics;
import com.payplatform.events.PaymentCompletedEvent;
import com.payplatform.events.PaymentFailedEvent;
import com.payplatform.events.PaymentInitiatedEvent;
import com.payplatform.payment.domain.Payment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class KafkaPaymentPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaPaymentPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishInitiated(Payment payment, String paymentMethod, String correlationId) {
        kafkaTemplate.send(EventTopics.PAYMENT_INITIATED, payment.getId(), new PaymentInitiatedEvent(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getMerchantId(),
                payment.getAmount().getAmount(),
                payment.getAmount().getCurrency(),
                paymentMethod,
                payment.getIdempotencyKey(),
                Instant.now(),
                correlationId
        ));
    }

    public void publishCompleted(Payment payment, String correlationId) {
        kafkaTemplate.send(EventTopics.PAYMENT_COMPLETED, payment.getId(), new PaymentCompletedEvent(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getMerchantId(),
                payment.getAmount().getAmount(),
                payment.getAmount().getCurrency(),
                Instant.now(),
                payment.getBankReference(),
                correlationId
        ));
    }

    public void publishFailed(Payment payment, String correlationId) {
        kafkaTemplate.send(EventTopics.PAYMENT_FAILED, payment.getId(), new PaymentFailedEvent(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getMerchantId(),
                payment.getAmount().getAmount(),
                payment.getAmount().getCurrency(),
                Instant.now(),
                payment.getFailureCode(),
                payment.getFailureReason(),
                correlationId
        ));
    }
}
