package com.payplatform.ledger.infrastructure;

import com.payplatform.ledger.domain.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, String> {
    List<JournalEntry> findByPaymentIdOrderByCreatedAtAsc(String paymentId);
}
