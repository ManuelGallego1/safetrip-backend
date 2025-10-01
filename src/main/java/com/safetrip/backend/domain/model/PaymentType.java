package com.safetrip.backend.domain.model;

import java.time.ZonedDateTime;

public class PaymentType {

    private final Long paymentTypeId;
    private final String name;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime updatedAt;

    public PaymentType(Long paymentTypeId,
                       String name,
                       ZonedDateTime createdAt,
                       ZonedDateTime updatedAt) {
        this.paymentTypeId = paymentTypeId;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getPaymentTypeId() {
        return paymentTypeId;
    }

    public String getName() {
        return name;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }
}