package com.payplatform.payment.infrastructure;

import com.payplatform.payment.application.BankGatewayPort;
import com.payplatform.payment.application.BankResponse;
import com.payplatform.payment.domain.Payment;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StubBankGatewayAdapter implements BankGatewayPort {
    @Override
    public BankResponse charge(Payment payment) {
        if (payment.getAmount().getAmount().doubleValue() > 100000) {
            return new BankResponse(false, null, "LIMIT_EXCEEDED", "Stub gateway declined amount above threshold");
        }
        return new BankResponse(true, "BANK-" + UUID.randomUUID(), null, null);
    }
}
