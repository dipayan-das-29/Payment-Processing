package com.payplatform.notification.application;

public record NotificationMessage(
        String subject,
        String body,
        String recipient
) {
}
