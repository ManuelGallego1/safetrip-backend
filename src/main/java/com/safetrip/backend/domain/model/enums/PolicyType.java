package com.safetrip.backend.domain.model.enums;

import lombok.Getter;

@Getter
public enum PolicyType {
    AP(1L, "Accidentes Personales", "AP"),
    HOTEL(2L, "Seguro Hotelero", "SH"),
    INNOMINADA(3L, "Póliza Innominada", "INN"),
    PAX(4L, "Póliza PAX", "PAX");

    private final Long id;
    private final String displayName;
    private final String code;

    PolicyType(Long id, String displayName, String code) {
        this.id = id;
        this.displayName = displayName;
        this.code = code;
    }

    public static PolicyType fromId(Long id) {
        for (PolicyType type : PolicyType.values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown policy type ID: " + id);
    }

    public static PolicyType fromCode(String code) {
        for (PolicyType type : PolicyType.values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown policy type code: " + code);
    }
}