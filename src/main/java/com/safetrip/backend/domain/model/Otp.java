package com.safetrip.backend.domain.model;

import java.time.ZonedDateTime;

public class Otp {

    private final Long otpId;
    private final User user;
    private final String code;
    private final ZonedDateTime expiration;
    private final boolean verified;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime updatedAt;

    public Otp(Long otpId,
               User user,
               String code,
               ZonedDateTime expiration,
               Boolean verified,
               ZonedDateTime createdAt,
               ZonedDateTime updatedAt) {
        this.otpId = otpId;
        this.user = user;
        this.code = code;
        this.expiration = expiration;
        this.verified = verified != null && verified;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getOtpId() {
        return otpId;
    }

    public User getUser() {
        return user;
    }

    public String getCode() {
        return code;
    }

    public ZonedDateTime getExpiration() {
        return expiration;
    }

    public boolean isVerified() {
        return verified;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean isExpired() {
        return expiration != null && expiration.isBefore(ZonedDateTime.now());
    }
}