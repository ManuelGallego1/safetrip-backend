package com.safetrip.backend.web.dto.response;

import com.safetrip.backend.domain.model.Discount;
import com.safetrip.backend.domain.model.enums.DiscountType;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class DiscountResponse {

    private Long discountId;
    private String name;
    private DiscountType type;
    private BigDecimal value;
    private Boolean active;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    // Constructor privado
    private DiscountResponse() {
    }

    // Constructor completo
    public DiscountResponse(Long discountId, String name, DiscountType type,
                            BigDecimal value, Boolean active,
                            ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.discountId = discountId;
        this.name = name;
        this.type = type;
        this.value = value;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Método factory para convertir desde el dominio
    public static DiscountResponse fromDomain(Discount discount) {
        return new DiscountResponse(
                discount.getDiscountId(),
                discount.getName(),
                discount.getType(),
                discount.getValue(),
                discount.getActive(),
                discount.getCreatedAt(),
                discount.getUpdatedAt()
        );
    }

    // Getters y Setters
    public Long getDiscountId() {
        return discountId;
    }

    public void setDiscountId(Long discountId) {
        this.discountId = discountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DiscountType getType() {
        return type;
    }

    public void setType(DiscountType type) {
        this.type = type;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}