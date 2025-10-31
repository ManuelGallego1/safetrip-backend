package com.safetrip.backend.domain.model.enums;

public enum RelationshipType {
    HOLDER("HOLDER"),
    BENEFICIARY("BENEFICIARY");
    private final String value;

    RelationshipType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RelationshipType fromValue(String value) {
        for (RelationshipType type : RelationshipType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown relationship type: " + value);
    }
}