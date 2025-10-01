package com.safetrip.backend.domain.model.enums;

public enum DocumentType {
    CC("CC"),
    PASSPORT("PASSPORT"),
    NIT("NIT"),
    NUIP("NUIP"),
    CE("CE");


    private final String value;

    DocumentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DocumentType fromValue(String value) {
        for (DocumentType type : DocumentType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown document type: " + value);
    }
}