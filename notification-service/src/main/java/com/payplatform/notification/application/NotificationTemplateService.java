package com.payplatform.notification.application;

import com.payplatform.events.PaymentCompletedEvent;
import com.payplatform.events.PaymentFailedEvent;
import org.springframework.stereotype.Service;

@Service
public class NotificationTemplateService {

    public NotificationMessage buildSuccessMessage(PaymentCompletedEvent event) {
        String subject = "Payment successful: " + event.paymentId();
        String body = "Payment " + event.paymentId() + " for order " + event.orderId() +
                " completed successfully with reference " + event.bankReference() + ".";
        return new NotificationMessage(subject, body, recipientFor(event.userId()));
    }

    public NotificationMessage buildFailureMessage(PaymentFailedEvent event) {
        String subject = "Payment failed: " + event.paymentId();
        String body = "Payment " + event.paymentId() + " for order " + event.orderId() +
                " failed due to " + event.failureReason() + ".";
        return new NotificationMessage(subject, body, recipientFor(event.userId()));
    }

    private String recipientFor(String userId) {
        return userId + "@example.com";
    }
}
