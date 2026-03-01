package com.safetrip.backend.domain.model.enums;

public enum NotificationType {
    WHATSAPP("WHATSAPP"),
    ALL("ALL"),
    EMAIL("EMAIL"),
    WHATSAPP_WITH_EMAIL_FALLBACK("WHATSAPP_WITH_EMAIL_FALLBACK");

    private final String value;

    NotificationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static NotificationType fromValue(String value) {
        for (NotificationType type : NotificationType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown notification type: " + value);
    }
}