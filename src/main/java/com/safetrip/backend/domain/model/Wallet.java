package com.safetrip.backend.domain.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public class Wallet {

    private final Long walletId;
    private final WalletType walletType;
    private final User user;
    private final BigDecimal total;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime updatedAt;

    public Wallet(Long walletId,
                  WalletType walletType,
                  User user,
                  BigDecimal total,
                  ZonedDateTime createdAt,
                  ZonedDateTime updatedAt) {
        this.walletId = walletId;
        this.walletType = walletType;
        this.user = user;
        this.total = total != null ? total : BigDecimal.ZERO;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getWalletId() {
        return walletId;
    }

    public WalletType getWalletType() {
        return walletType;
    }

    public User getUser() {
        return user;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }
}