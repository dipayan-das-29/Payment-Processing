package com.payplatform.ledger.application;

import com.payplatform.ledger.domain.EntryType;
import com.payplatform.ledger.domain.JournalEntry;

import java.math.BigDecimal;
import java.time.Instant;

public record JournalEntryResponse(
        String id,
        String paymentId,
        EntryType entryType,
        String accountId,
        BigDecimal amount,
        String currency,
        Instant createdAt
) {
    public static JournalEntryResponse from(JournalEntry entry) {
        return new JournalEntryResponse(
                entry.getId(),
                entry.getPaymentId(),
                entry.getEntryType(),
                entry.getAccountId(),
                entry.getAmount(),
                entry.getCurrency(),
                entry.getCreatedAt()
        );
    }
}
