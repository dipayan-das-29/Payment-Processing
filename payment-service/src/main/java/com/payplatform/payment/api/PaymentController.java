package com.payplatform.payment.api;

import com.payplatform.payment.api.dto.CreatePaymentRequest;
import com.payplatform.payment.application.PaymentRequest;
import com.payplatform.payment.application.PaymentResponse;
import com.payplatform.payment.application.PaymentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public PaymentResponse create(@Valid @RequestBody CreatePaymentRequest request) {
        return paymentService.initiatePayment(new PaymentRequest(
                request.userId(),
                request.merchantId(),
                request.orderId(),
                request.amount(),
                request.currency(),
                request.paymentMethod(),
                request.idempotencyKey(),
                request.correlationId() == null || request.correlationId().isBlank() ? UUID.randomUUID().toString() : request.correlationId()
        ));
    }

    @GetMapping("/{id}")
    public PaymentResponse getById(@PathVariable String id) {
        return paymentService.getById(id);
    }

    @GetMapping
    public List<PaymentResponse> getByUserId(@RequestParam String userId) {
        return paymentService.getByUserId(userId);
    }

    @PostMapping("/{id}/refund")
    public PaymentResponse refund(@PathVariable String id) {
        return paymentService.refund(id);
    }
}
