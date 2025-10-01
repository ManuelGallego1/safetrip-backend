package com.safetrip.backend.infrastructure.persistence.mapper;

import com.safetrip.backend.domain.model.WalletTransaction;
import com.safetrip.backend.infrastructure.persistence.entity.WalletTransactionEntity;

public class WalletTransactionMapper {

    public static WalletTransaction toDomain(WalletTransactionEntity entity) {
        if (entity == null) return null;

        return new WalletTransaction(
                entity.getWalletTransactionId(),
                PaymentMapper.toDomain(entity.getPayment()),
                WalletMapper.toDomain(entity.getWallet()),
                entity.getIncome(),
                entity.getTotal(),
                entity.getBalance(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static WalletTransactionEntity toEntity(WalletTransaction domain) {
        if (domain == null) return null;

        WalletTransactionEntity entity = new WalletTransactionEntity();
        entity.setWalletTransactionId(domain.getWalletTransactionId());
        entity.setPayment(PaymentMapper.toEntity(domain.getPayment()));
        entity.setWallet(WalletMapper.toEntity(domain.getWallet()));
        entity.setIncome(domain.getIncome());
        entity.setTotal(domain.getTotal());
        entity.setBalance(domain.getBalance());
        entity.setDescription(domain.getDescription());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}