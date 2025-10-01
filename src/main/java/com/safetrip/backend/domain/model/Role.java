package com.safetrip.backend.domain.model;

import java.time.ZonedDateTime;

public class Role {

    private final Long roleId;
    private final String name;
    private final String description;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime updatedAt;

    public Role(Long roleId,
                String name,
                String description,
                ZonedDateTime createdAt,
                ZonedDateTime updatedAt) {
        this.roleId = roleId;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getRoleId() {
        return roleId;
    }

    public String getName() {
        return name;
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