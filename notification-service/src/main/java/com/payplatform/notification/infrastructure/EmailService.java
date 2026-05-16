package com.payplatform.notification.infrastructure;

import com.payplatform.notification.application.NotificationMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendAsync(NotificationMessage notificationMessage) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(notificationMessage.recipient());
        message.setSubject(notificationMessage.subject());
        message.setText(notificationMessage.body());
        message.setFrom("noreply@payplatform.dev");
        mailSender.send(message);
    }
}
