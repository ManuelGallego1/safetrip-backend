package com.safetrip.backend.domain.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class Wallet {

    private final Long walletId;
    private final WalletType walletType;
    private final User user;
    private final BigDecimal total;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime updatedAt;
    private final ZonedDateTime startDate;
    private final ZonedDateTime endDate;
    private final String transactionId;

    public Wallet(Long walletId,
                  WalletType walletType,
                  User user,
                  BigDecimal total,
                  ZonedDateTime createdAt,
                  ZonedDateTime updatedAt,
                  ZonedDateTime startDate,
                  ZonedDateTime endDate,
                  String transactionId) {
        this.walletId = walletId;
        this.walletType = walletType;
        this.user = user;
        this.total = total != null ? total : BigDecimal.ZERO;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.startDate = startDate;
        this.endDate = endDate;
        this.transactionId = transactionId;
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

    public ZonedDateTime getStartDate() { return startDate; }

    public ZonedDateTime getEndDate() { return endDate; }

    public String getTransactionId() { return transactionId; }
}