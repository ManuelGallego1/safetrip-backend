package com.safetrip.backend.infrastructure.persistence.mapper;

import com.safetrip.backend.domain.model.Wallet;
import com.safetrip.backend.infrastructure.persistence.entity.WalletEntity;

public class WalletMapper {

    public static Wallet toDomain(WalletEntity entity) {
        if (entity == null) return null;

        return new Wallet(
                entity.getWalletId(),
                WalletTypeMapper.toDomain(entity.getWalletType()),
                UserMapper.toDomain(entity.getUser()),
                entity.getTotal(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static WalletEntity toEntity(Wallet domain) {
        if (domain == null) return null;

        WalletEntity entity = new WalletEntity();
        entity.setWalletId(domain.getWalletId());
        entity.setWalletType(WalletTypeMapper.toEntity(domain.getWalletType()));
        entity.setUser(UserMapper.toEntity(domain.getUser()));
        entity.setTotal(domain.getTotal());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}