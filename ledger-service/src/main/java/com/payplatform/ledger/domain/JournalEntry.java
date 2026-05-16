package com.payplatform.ledger.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "journal_entries")
public class JournalEntry {
    @Id
    private String id;

    @Column(nullable = false)
    private String paymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryType entryType;

    @Column(nullable = false)
    private String accountId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private Instant createdAt;

    protected JournalEntry() {
    }

    public static JournalEntry of(String paymentId, EntryType entryType, String accountId, BigDecimal amount, String currency) {
        JournalEntry entry = new JournalEntry();
        entry.id = UUID.randomUUID().toString();
        entry.paymentId = paymentId;
        entry.entryType = entryType;
        entry.accountId = accountId;
        entry.amount = amount;
        entry.currency = currency;
        entry.createdAt = Instant.now();
        return entry;
    }

    public String getId() { return id; }
    public String getPaymentId() { return paymentId; }
    public EntryType getEntryType() { return entryType; }
    public String getAccountId() { return accountId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public Instant getCreatedAt() { return createdAt; }
}
