package com.safetrip.backend.domain.model.enums;

public enum DiscountType {
    PERCENTAGE("percentage"),
    FIXED("fixed");

    private final String value;

    DiscountType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DiscountType fromValue(String value) {
        for (DiscountType type : DiscountType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown discount type: " + value);
    }
}