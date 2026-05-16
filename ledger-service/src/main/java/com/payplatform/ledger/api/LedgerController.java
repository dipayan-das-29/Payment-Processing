package com.payplatform.ledger.api;

import com.payplatform.ledger.application.JournalEntryResponse;
import com.payplatform.ledger.application.JournalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/ledger")
public class LedgerController {
    private final JournalService journalService;

    public LedgerController(JournalService journalService) {
        this.journalService = journalService;
    }

    @GetMapping("/payments/{paymentId}")
    public List<JournalEntryResponse> getEntriesByPaymentId(@PathVariable String paymentId) {
        return journalService.getByPaymentId(paymentId);
    }
}
