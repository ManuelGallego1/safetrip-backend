package com.safetrip.backend.domain.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class WalletTransaction {

    private final Long walletTransactionId;
    private final Payment payment;
    private final Wallet wallet;
    private final Boolean income;
    private final BigDecimal total;
    private final BigDecimal balance;
    private final String description;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime updatedAt;

    public WalletTransaction(Long walletTransactionId,
                             Payment payment,
                             Wallet wallet,
                             Boolean income,
                             BigDecimal total,
                             BigDecimal balance,
                             String description,
                             ZonedDateTime createdAt,
                             ZonedDateTime updatedAt) {
        this.walletTransactionId = walletTransactionId;
        this.payment = payment;
        this.wallet = wallet;
        this.income = income;
        this.total = total != null ? total : BigDecimal.ZERO;
        this.balance = balance != null ? balance : BigDecimal.ZERO;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getWalletTransactionId() {
        return walletTransactionId;
    }

    public Payment getPayment() {
        return payment;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public Boolean getIncome() {
        return income;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public BigDecimal getBalance() {
        return balance;
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