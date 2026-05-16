package com.payplatform.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {
    @Id
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String merchantId;

    @Column(nullable = false)
    private String orderId;

    @Embedded
    private Money amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    private String bankReference;
    private String failureCode;
    private String failureReason;
    private Instant createdAt;
    private Instant updatedAt;
    private int retryCount;

    protected Payment() {
    }

    public static Payment initiate(String userId, String merchantId, String orderId, Money amount, String idempotencyKey) {
        Payment payment = new Payment();
        payment.id = UUID.randomUUID().toString();
        payment.userId = userId;
        payment.merchantId = merchantId;
        payment.orderId = orderId;
        payment.amount = amount;
        payment.idempotencyKey = idempotencyKey;
        payment.status = PaymentStatus.INITIATED;
        payment.createdAt = Instant.now();
        payment.updatedAt = payment.createdAt;
        payment.retryCount = 0;
        return payment;
    }

    public void markProcessing() {
        this.status = PaymentStatus.PROCESSING;
        this.updatedAt = Instant.now();
    }

    public void complete(String bankReference) {
        this.status = PaymentStatus.COMPLETED;
        this.bankReference = bankReference;
        this.updatedAt = Instant.now();
    }

    public void fail(String failureCode, String failureReason) {
        this.status = PaymentStatus.FAILED;
        this.failureCode = failureCode;
        this.failureReason = failureReason;
        this.updatedAt = Instant.now();
        this.retryCount++;
    }

    public void refundInitiated() {
        this.status = PaymentStatus.REFUND_INITIATED;
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getMerchantId() { return merchantId; }
    public String getOrderId() { return orderId; }
    public Money getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getBankReference() { return bankReference; }
    public String getFailureCode() { return failureCode; }
    public String getFailureReason() { return failureReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public int getRetryCount() { return retryCount; }
}
