package com.safetrip.backend.domain.model.enums;

public enum RoleType {
    ADMIN("ADMIN"),
    SOPORTE("SOPORTE"),
    CLIENTE("CLIENTE"),
    ASESOR("ASESOR");

    private final String value;

    RoleType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RoleType fromValue(String value) {
        for (RoleType type : RoleType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown role type: " + value);
    }
}