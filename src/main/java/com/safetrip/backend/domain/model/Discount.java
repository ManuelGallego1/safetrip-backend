package com.safetrip.backend.domain.model;

import com.safetrip.backend.domain.model.enums.DiscountType;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class Discount {
    private final Long discountId;
    private final String name;
    private final DiscountType type;
    private final BigDecimal value;
    private final Boolean active;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime updatedAt;

    // Constructor completo
    public Discount(Long discountId, String name, DiscountType type, BigDecimal value,
                    Boolean active, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.discountId = discountId;
        this.name = name;
        this.type = type;
        this.value = value;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Constructor para nuevo descuento (sin ID)
    public static Discount create(String name, DiscountType type, BigDecimal value) {
        return new Discount(
                null,
                name,
                type,
                value,
                true,
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );
    }

    // Métodos de actualización (inmutables)
    public Discount updateName(String newName) {
        return new Discount(
                this.discountId,
                newName,
                this.type,
                this.value,
                this.active,
                this.createdAt,
                ZonedDateTime.now()
        );
    }

    public Discount updateType(DiscountType newType) {
        return new Discount(
                this.discountId,
                this.name,
                newType,
                this.value,
                this.active,
                this.createdAt,
                ZonedDateTime.now()
        );
    }

    public Discount updateValue(BigDecimal newValue) {
        return new Discount(
                this.discountId,
                this.name,
                this.type,
                newValue,
                this.active,
                this.createdAt,
                ZonedDateTime.now()
        );
    }

    public Discount updateActive(Boolean newActive) {
        return new Discount(
                this.discountId,
                this.name,
                this.type,
                this.value,
                newActive,
                this.createdAt,
                ZonedDateTime.now()
        );
    }

    // Getters
    public Long getDiscountId() { return discountId; }
    public String getName() { return name; }
    public DiscountType getType() { return type; }
    public BigDecimal getValue() { return value; }
    public Boolean getActive() { return active; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }
}