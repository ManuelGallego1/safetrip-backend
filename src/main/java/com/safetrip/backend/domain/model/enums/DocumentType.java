package com.safetrip.backend.domain.model.enums;

public enum DocumentType {
    CC("CC"),
    PASSPORT("Pasaporte"),
    NIT("NIT"),
    NUIP("NUIP"),
    CE("CE");

    private final String displayName;

    DocumentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCode() {
        return this.name();
    }

    /**
     * Busca por código (CC, PASSPORT, etc.) o por nombre en español
     */
    public static DocumentType fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de documento no puede estar vacío");
        }

        String normalizedValue = value.trim().toUpperCase();

        // Buscar por código (CC, PASSPORT, etc.)
        for (DocumentType type : DocumentType.values()) {
            if (type.name().equals(normalizedValue)) {
                return type;
            }
        }

        // Buscar por nombre en español
        for (DocumentType type : DocumentType.values()) {
            if (type.displayName.equalsIgnoreCase(value.trim())) {
                return type;
            }
        }

        throw new IllegalArgumentException("Tipo de documento desconocido: " + value +
                ". Tipos válidos: CC, PASSPORT, NIT, NUIP, CE");
    }
}