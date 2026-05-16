package com.payplatform.payment.application;

import com.payplatform.payment.domain.Money;
import com.payplatform.payment.domain.Payment;
import com.payplatform.payment.infrastructure.KafkaPaymentPublisher;
import com.payplatform.payment.infrastructure.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final IdempotencyService idempotencyService;
    private final BankGatewayPort bankGatewayPort;
    private final KafkaPaymentPublisher kafkaPaymentPublisher;

    public PaymentService(PaymentRepository paymentRepository,
                          IdempotencyService idempotencyService,
                          BankGatewayPort bankGatewayPort,
                          KafkaPaymentPublisher kafkaPaymentPublisher) {
        this.paymentRepository = paymentRepository;
        this.idempotencyService = idempotencyService;
        this.bankGatewayPort = bankGatewayPort;
        this.kafkaPaymentPublisher = kafkaPaymentPublisher;
    }

    public PaymentResponse initiatePayment(PaymentRequest request) {
        return idempotencyService.getOrExecute(request.idempotencyKey(), () -> {
            Payment payment = Payment.initiate(
                    request.userId(),
                    request.merchantId(),
                    request.orderId(),
                    new Money(request.amount(), request.currency()),
                    request.idempotencyKey()
            );
            payment.markProcessing();
            paymentRepository.save(payment);
            kafkaPaymentPublisher.publishInitiated(payment, request.paymentMethod(), request.correlationId());

            BankResponse bankResponse = bankGatewayPort.charge(payment);
            if (bankResponse.approved()) {
                payment.complete(bankResponse.reference());
                paymentRepository.save(payment);
                kafkaPaymentPublisher.publishCompleted(payment, request.correlationId());
            } else {
                payment.fail(bankResponse.failureCode(), bankResponse.failureReason());
                paymentRepository.save(payment);
                kafkaPaymentPublisher.publishFailed(payment, request.correlationId());
            }
            return PaymentResponse.from(payment);
        });
    }

    @Transactional(readOnly = true)
    public PaymentResponse getById(String id) {
        return paymentRepository.findById(id)
                .map(PaymentResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + id));
    }

    @Transactional(readOnly = true)
    public java.util.List<PaymentResponse> getByUserId(String userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(PaymentResponse::from)
                .toList();
    }

    public PaymentResponse refund(String id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + id));
        payment.refundInitiated();
        paymentRepository.save(payment);
        return PaymentResponse.from(payment);
    }
}
