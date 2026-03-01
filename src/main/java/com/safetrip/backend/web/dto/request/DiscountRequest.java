package com.safetrip.backend.web.dto.request;

import com.safetrip.backend.domain.model.enums.DiscountType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class DiscountRequest {

    @NotBlank(message = "El nombre del descuento es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String name;

    @NotNull(message = "El tipo de descuento es obligatorio")
    private DiscountType type;

    @NotNull(message = "El valor del descuento es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El valor debe ser mayor a 0")
    private BigDecimal value;

    private Boolean active;

    // Constructores
    public DiscountRequest() {
    }

    public DiscountRequest(String name, DiscountType type, BigDecimal value, Boolean active) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.active = active;
    }

    // Getters y Setters
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
        return active != null ? active : true;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}