package com.safetrip.backend.infrastructure.persistence.mapper;

import com.safetrip.backend.domain.model.WalletType;
import com.safetrip.backend.infrastructure.persistence.entity.WalletTypeEntity;

public class WalletTypeMapper {

    private WalletTypeMapper() {
    }

    public static WalletType toDomain(WalletTypeEntity entity) {
        if (entity == null) {
            return null;
        }

        return new WalletType(
                entity.getWalletTypeId(),
                entity.getName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static WalletTypeEntity toEntity(WalletType domain) {
        if (domain == null) {
            return null;
        }

        return WalletTypeEntity.builder()
                .walletTypeId(domain.getWalletTypeId())
                .name(domain.getName())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}