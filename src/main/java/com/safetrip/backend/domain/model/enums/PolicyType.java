package com.safetrip.backend.domain.model.enums;

public enum PolicyType {
    AP("AP"),
    HOTEL("HOTEL");

    private final String value;

    PolicyType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PolicyType fromValue(String value) {
        for (PolicyType type : PolicyType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown policy type: " + value);
    }
}