package com.payplatform.payment.application;

import com.payplatform.payment.domain.PaymentStatus;
import com.payplatform.payment.infrastructure.KafkaPaymentPublisher;
import com.payplatform.payment.infrastructure.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceIdempotencyTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private BankGatewayPort bankGatewayPort;

    @Mock
    private KafkaPaymentPublisher kafkaPaymentPublisher;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void shouldReturnCachedResultForDuplicateIdempotencyKey() {
        PaymentRequest request = new PaymentRequest(
                "user-10", "merchant-10", "order-10", new BigDecimal("400.00"),
                "INR", "CARD", "idem-duplicate", "corr-10"
        );

        PaymentResponse cached = new PaymentResponse(
                "payment-10",
                "order-10",
                "user-10",
                "merchant-10",
                new BigDecimal("400.00"),
                "INR",
                PaymentStatus.COMPLETED,
                "BANK-10",
                null,
                null,
                null,
                null
        );

        when(idempotencyService.getOrExecute(any(), any())).thenReturn(cached);

        PaymentResponse result = paymentService.initiatePayment(request);

        assertEquals("payment-10", result.paymentId());
        assertEquals(PaymentStatus.COMPLETED, result.status());
    }
}