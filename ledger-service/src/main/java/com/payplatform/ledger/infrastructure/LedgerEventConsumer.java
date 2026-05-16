package com.payplatform.ledger.infrastructure;

import com.payplatform.events.PaymentCompletedEvent;
import com.payplatform.ledger.application.JournalService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class LedgerEventConsumer {
    private final JournalService journalService;

    public LedgerEventConsumer(JournalService journalService) {
        this.journalService = journalService;
    }

    @KafkaListener(topics = "payment.completed", groupId = "ledger-service")
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        journalService.recordTransfer(
                event.paymentId(),
                event.userId(),
                event.merchantId(),
                event.amount(),
                event.currency()
        );
    }
}
