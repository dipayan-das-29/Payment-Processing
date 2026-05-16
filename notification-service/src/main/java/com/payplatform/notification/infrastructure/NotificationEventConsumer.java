package com.payplatform.notification.infrastructure;

import com.payplatform.events.PaymentCompletedEvent;
import com.payplatform.events.PaymentFailedEvent;
import com.payplatform.notification.application.NotificationTemplateService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventConsumer {
    private final NotificationTemplateService templateService;
    private final EmailService emailService;

    public NotificationEventConsumer(NotificationTemplateService templateService, EmailService emailService) {
        this.templateService = templateService;
        this.emailService = emailService;
    }

    @KafkaListener(topics = "payment.completed", groupId = "notification-service")
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        emailService.sendAsync(templateService.buildSuccessMessage(event));
    }

    @KafkaListener(topics = "payment.failed", groupId = "notification-service")
    public void onPaymentFailed(PaymentFailedEvent event) {
        emailService.sendAsync(templateService.buildFailureMessage(event));
    }
}
