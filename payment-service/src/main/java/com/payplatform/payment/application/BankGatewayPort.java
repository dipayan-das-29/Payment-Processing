package com.payplatform.payment.application;

import com.payplatform.payment.domain.Payment;

public interface BankGatewayPort {
    BankResponse charge(Payment payment);
}
