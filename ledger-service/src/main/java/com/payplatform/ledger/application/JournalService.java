package com.payplatform.ledger.application;

import com.payplatform.ledger.domain.EntryType;
import com.payplatform.ledger.domain.JournalEntry;
import com.payplatform.ledger.infrastructure.JournalEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class JournalService {
    private final JournalEntryRepository journalEntryRepository;

    public JournalService(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    public void recordTransfer(String paymentId, String buyerAccountId, String merchantAccountId, BigDecimal amount, String currency) {
        JournalEntry debit = JournalEntry.of(paymentId, EntryType.DEBIT, buyerAccountId, amount, currency);
        JournalEntry credit = JournalEntry.of(paymentId, EntryType.CREDIT, merchantAccountId, amount, currency);
        journalEntryRepository.saveAll(List.of(debit, credit));
    }

    @Transactional(readOnly = true)
    public List<JournalEntryResponse> getByPaymentId(String paymentId) {
        return journalEntryRepository.findByPaymentIdOrderByCreatedAtAsc(paymentId)
                .stream()
                .map(JournalEntryResponse::from)
                .toList();
    }
}
