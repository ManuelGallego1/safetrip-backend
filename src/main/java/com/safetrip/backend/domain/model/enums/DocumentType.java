package com.safetrip.backend.domain.model.enums;

public enum DocumentType {
    CC("CC", "Cédula", "1"),
    CE("CE", "Cédula de extranjería", "2"),
    PASSPORT("Pasaporte", "Pasaporte", "3"),
    NUIP("NUIP", "TI O NUIP", "4"),
    NIT("NIT", "NIT", null);

    private final String code;
    private final String displayName;
    private final String excelCode; // Código numérico del Excel

    DocumentType(String code, String displayName, String excelCode) {
        this.code = code;
        this.displayName = displayName;
        this.excelCode = excelCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCode() {
        return code;
    }

    public String getExcelCode() {
        return excelCode;
    }

    /**
     * Busca por código (CC, PASSPORT, etc.), nombre en español o código numérico del Excel
     */
    public static DocumentType fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de documento no puede estar vacío");
        }

        String normalizedValue = value.trim();

        // 1. Buscar por código numérico del Excel (1, 2, 3, 4)
        for (DocumentType type : DocumentType.values()) {
            if (type.excelCode != null && type.excelCode.equals(normalizedValue)) {
                return type;
            }
        }

        // 2. Buscar por código (CC, PASSPORT, NUIP, CE, NIT)
        String upperValue = normalizedValue.toUpperCase();
        for (DocumentType type : DocumentType.values()) {
            if (type.code.equals(upperValue)) {
                return type;
            }
        }

        // 3. Buscar por nombre en español (case insensitive)
        for (DocumentType type : DocumentType.values()) {
            if (type.displayName.equalsIgnoreCase(normalizedValue)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Tipo de documento desconocido: '" + value +
                "'. Tipos válidos: 1 (Cédula), 2 (CE), 3 (Pasaporte), 4 (TI/NUIP), CC, PASSPORT, CE, NUIP, NIT");
    }
}