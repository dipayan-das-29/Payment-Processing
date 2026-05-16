package com.payplatform.payment.application;

import com.payplatform.payment.domain.Money;
import com.payplatform.payment.domain.Payment;
import com.payplatform.payment.domain.PaymentStatus;
import com.payplatform.payment.infrastructure.KafkaPaymentPublisher;
import com.payplatform.payment.infrastructure.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

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
    void shouldCompletePaymentWhenBankApproves() {
        PaymentRequest request = new PaymentRequest(
                "user-1", "merchant-1", "order-1", new BigDecimal("150.00"),
                "INR", "CARD", "idem-1", "corr-1"
        );

        doAnswer(invocation -> {
            java.util.function.Supplier<PaymentResponse> supplier = invocation.getArgument(1);
            return supplier.get();
        }).when(idempotencyService).getOrExecute(any(), any());

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bankGatewayPort.charge(any(Payment.class))).thenReturn(new BankResponse(true, "BANK-REF-1", null, null));

        PaymentResponse response = paymentService.initiatePayment(request);

        assertEquals(PaymentStatus.COMPLETED, response.status());
        assertEquals("BANK-REF-1", response.bankReference());
        verify(kafkaPaymentPublisher).publishInitiated(any(Payment.class), any(), any());
        verify(kafkaPaymentPublisher).publishCompleted(any(Payment.class), any());
    }

    @Test
    void shouldSetStatusToFailedWhenBankRejects() {
        PaymentRequest request = new PaymentRequest(
                "user-2", "merchant-2", "order-2", new BigDecimal("250000.00"),
                "INR", "CARD", "idem-2", "corr-2"
        );

        doAnswer(invocation -> {
            java.util.function.Supplier<PaymentResponse> supplier = invocation.getArgument(1);
            return supplier.get();
        }).when(idempotencyService).getOrExecute(any(), any());

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bankGatewayPort.charge(any(Payment.class))).thenReturn(new BankResponse(false, null, "LIMIT_EXCEEDED", "Declined"));

        PaymentResponse response = paymentService.initiatePayment(request);

        assertEquals(PaymentStatus.FAILED, response.status());
        assertEquals("LIMIT_EXCEEDED", response.failureCode());
        verify(kafkaPaymentPublisher).publishFailed(any(Payment.class), any());
    }

    @Test
    void shouldThrowWhenPaymentNotFound() {
        when(paymentRepository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> paymentService.getById("missing"));
    }

    @Test
    void shouldReturnPaymentsByUserId() {
        Payment payment = Payment.initiate("user-3", "merchant-3", "order-3", new Money(new BigDecimal("99.99"), "INR"), "idem-3");
        payment.complete("BANK-REF-3");
        when(paymentRepository.findByUserIdOrderByCreatedAtDesc("user-3")).thenReturn(List.of(payment));

        List<PaymentResponse> results = paymentService.getByUserId("user-3");

        assertEquals(1, results.size());
        assertEquals(PaymentStatus.COMPLETED, results.get(0).status());
        verify(bankGatewayPort, never()).charge(any());
    }
}
