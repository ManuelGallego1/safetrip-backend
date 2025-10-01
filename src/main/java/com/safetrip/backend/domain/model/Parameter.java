package com.safetrip.backend.domain.model;

import java.time.ZonedDateTime;

public class Parameter {

    private final Long parameterId;
    private final String description;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime updatedAt;

    public Parameter(Long parameterId,
                     String description,
                     ZonedDateTime createdAt,
                     ZonedDateTime updatedAt) {
        this.parameterId = parameterId;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getParameterId() {
        return parameterId;
    }

    public String getDescription() {
        return description;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }
}