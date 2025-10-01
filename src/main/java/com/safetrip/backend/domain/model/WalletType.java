package com.safetrip.backend.domain.model;

import java.time.ZonedDateTime;
import java.util.List;

public class WalletType {

    private final Long walletTypeId;
    private final String name;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime updatedAt;

    public WalletType(Long walletTypeId,
                      String name,
                      ZonedDateTime createdAt,
                      ZonedDateTime updatedAt) {
        this.walletTypeId = walletTypeId;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getWalletTypeId() {
        return walletTypeId;
    }

    public String getName() {
        return name;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }
}