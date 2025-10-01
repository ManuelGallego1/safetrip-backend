package com.safetrip.backend.domain.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class Discount {
    private final Long discountId;
    private final String name;
    private final String type;
    private final BigDecimal value;
    private final Boolean active;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime updatedAt;

    public Discount(Long discountId, String name, String type, BigDecimal value, Boolean active,  ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.discountId = discountId;
        this.name = name;
        this.type = type;
        this.value = value;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getDiscountId() { return discountId; }
    public String getName() { return name; }
    public String getType() { return type; }
    public BigDecimal getValue() { return value; }
    public Boolean getActive() { return active; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }
}