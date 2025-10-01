package com.safetrip.backend.infrastructure.persistence.mapper;

import com.safetrip.backend.domain.model.PaymentType;
import com.safetrip.backend.infrastructure.persistence.entity.PaymentTypeEntity;

public class PaymentTypeMapper {

    public static PaymentType toDomain(PaymentTypeEntity entity) {
        if (entity == null) return null;

        return new PaymentType(
                entity.getPaymentTypeId(),
                entity.getName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static PaymentTypeEntity toEntity(PaymentType domain) {
        if (domain == null) return null;

        PaymentTypeEntity entity = new PaymentTypeEntity();
        entity.setPaymentTypeId(domain.getPaymentTypeId());
        entity.setName(domain.getName());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}