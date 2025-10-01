package com.safetrip.backend.domain.model;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

public class User {

    private final Long userId;
    private final Person person;
    private final String email;
    private final String phone;
    private final String passwordHash;
    private final Role role;
    private final Boolean isActive;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime updatedAt;

    public User(Long userId,
                Person person,
                String email,
                String phone,
                String passwordHash,
                Role role,
                Boolean isActive,
                ZonedDateTime createdAt,
                ZonedDateTime updatedAt) {
        this.userId = userId;
        this.person = person;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isActive = isActive != null ? isActive : Boolean.TRUE;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public Person getPerson() {
        return person;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }
}