package com.safetrip.backend.domain.model.enums;

public enum PolicyDataSource {
    MANUAL("Manual"),
    EXCEL("Excel"),
    IMAGE("Imagen");

    private final String displayName;

    PolicyDataSource(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}