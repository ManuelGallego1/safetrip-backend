package com.safetrip.backend.infrastructure.persistence.mapper;

import com.safetrip.backend.domain.model.PolicyPayment;
import com.safetrip.backend.infrastructure.persistence.entity.PolicyPaymentEntity;

public class PolicyPaymentMapper {

    public static PolicyPayment toDomain(PolicyPaymentEntity entity) {
        if (entity == null) return null;

        return new PolicyPayment(
                entity.getPolicyPaymentId(),
                PaymentMapper.toDomain(entity.getPayment()),
                PolicyMapper.toDomain(entity.getPolicy()),
                entity.getAppliedAmount(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static PolicyPaymentEntity toEntity(PolicyPayment domain) {
        if (domain == null) return null;

        PolicyPaymentEntity entity = new PolicyPaymentEntity();
        entity.setPolicyPaymentId(domain.getPolicyPaymentId());
        entity.setPayment(PaymentMapper.toEntity(domain.getPayment()));
        entity.setPolicy(PolicyMapper.toEntity(domain.getPolicy()));
        entity.setAppliedAmount(domain.getAppliedAmount());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}