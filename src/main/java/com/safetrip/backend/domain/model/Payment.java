package com.safetrip.backend.domain.model;

import com.safetrip.backend.domain.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class Payment {

    private final Long paymentId;
    private final PaymentType paymentType;
    private final PaymentStatus status;
    private final String transactionId;
    private final BigDecimal amount;
    private final User user;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime updatedAt;

    public Payment(Long paymentId,
                   PaymentType paymentType,
                   PaymentStatus status,
                   String transactionId,
                   BigDecimal amount,
                   User user,
                   ZonedDateTime createdAt,
                   ZonedDateTime updatedAt) {
        this.paymentId = paymentId;
        this.paymentType = paymentType;
        this.status = status;
        this.transactionId = transactionId;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.user = user;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public User getUser() {
        return user;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }
}