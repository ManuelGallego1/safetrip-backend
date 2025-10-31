package com.safetrip.backend.domain.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class PolicyType {

    private final Long policyTypeId;
    private final String name;
    private final BigDecimal baseValue;
    private final Boolean active;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime updatedAt;

    public PolicyType(Long policyTypeId,
                      String name,
                      BigDecimal baseValue,
                      Boolean active,
                      ZonedDateTime createdAt,
                      ZonedDateTime updatedAt) {
        this.policyTypeId = policyTypeId;
        this.name = name;
        this.baseValue = baseValue != null ? baseValue : BigDecimal.ZERO;
        this.active = active != null ? active : Boolean.TRUE;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getPolicyTypeId() {
        return policyTypeId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getBaseValue() {
        return baseValue;
    }

    public Boolean getActive() {
        return active;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }
}