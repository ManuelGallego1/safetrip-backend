package com.safetrip.backend.infrastructure.persistence.mapper;

import com.safetrip.backend.domain.model.Payment;
import com.safetrip.backend.infrastructure.persistence.entity.PaymentEntity;

public class PaymentMapper {

    public static Payment toDomain(PaymentEntity entity) {
        if (entity == null) return null;

        return new Payment(
                entity.getPaymentId(),
                PaymentTypeMapper.toDomain(entity.getPaymentType()),
                entity.getStatus(),
                entity.getTransactionId(),
                entity.getAmount(),
                UserMapper.toDomain(entity.getUser()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static PaymentEntity toEntity(Payment domain) {
        if (domain == null) return null;

        PaymentEntity entity = new PaymentEntity();
        entity.setPaymentId(domain.getPaymentId());
        entity.setPaymentType(PaymentTypeMapper.toEntity(domain.getPaymentType()));
        entity.setStatus(domain.getStatus());
        entity.setTransactionId(domain.getTransactionId());
        entity.setAmount(domain.getAmount());
        entity.setUser(UserMapper.toEntity(domain.getUser()));
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}