package com.safetrip.backend.domain.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class PolicyPayment {

    private final Long policyPaymentId;
    private final Payment payment;
    private final Policy policy;
    private final BigDecimal appliedAmount;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime updatedAt;

    public PolicyPayment(Long policyPaymentId,
                         Payment payment,
                         Policy policy,
                         BigDecimal appliedAmount,
                         ZonedDateTime createdAt,
                         ZonedDateTime updatedAt) {
        this.policyPaymentId = policyPaymentId;
        this.payment = payment;
        this.policy = policy;
        this.appliedAmount = appliedAmount != null ? appliedAmount : BigDecimal.ZERO;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getPolicyPaymentId() {
        return policyPaymentId;
    }

    public Payment getPayment() {
        return payment;
    }

    public Policy getPolicy() {
        return policy;
    }

    public BigDecimal getAppliedAmount() {
        return appliedAmount;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }
}