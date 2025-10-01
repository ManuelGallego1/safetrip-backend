package com.safetrip.backend.domain.model.enums;

public enum RoleName {
    ADMIN("ADMIN"),
    SUPPORT("SUPPORT"),
    CUSTOMER("CUSTOMER"),
    ADVISOR("ADVISOR");

    private final String value;

    RoleName(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RoleName fromValue(String value) {
        for (RoleName type : RoleName.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown role type: " + value);
    }
}