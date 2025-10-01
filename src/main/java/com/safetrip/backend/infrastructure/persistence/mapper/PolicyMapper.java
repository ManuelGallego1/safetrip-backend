package com.safetrip.backend.infrastructure.persistence.mapper;

import com.safetrip.backend.domain.model.*;
import com.safetrip.backend.infrastructure.persistence.entity.*;

import java.util.stream.Collectors;

public class PolicyMapper {

    public static Policy toDomain(PolicyEntity entity) {
        if (entity == null) return null;

        return new Policy(
                entity.getPolicyId(),
                PolicyTypeMapper.toDomain(entity.getPolicyType()),
                entity.getPersonCount(),
                entity.getUnitPriceWithDiscount(),
                DiscountMapper.toDomain(entity.getDiscount()),
                entity.getPolicyNumber(),
                UserMapper.toDomain(entity.getCreatedByUser()),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getPolicyDetails() != null
                        ? entity.getPolicyDetails().stream()
                        .map(PolicyDetailMapper::toDomain)
                        .collect(Collectors.toList())
                        : null,
                entity.getPolicyPayments() != null
                        ? entity.getPolicyPayments().stream()
                        .map(PolicyPaymentMapper::toDomain)
                        .collect(Collectors.toList())
                        : null
        );
    }

    public static PolicyEntity toEntity(Policy domain) {
        if (domain == null) return null;

        PolicyEntity entity = new PolicyEntity();
        entity.setPolicyId(domain.getPolicyId());
        entity.setPolicyType(PolicyTypeMapper.toEntity(domain.getPolicyType()));
        entity.setPersonCount(domain.getPersonCount());
        entity.setUnitPriceWithDiscount(domain.getUnitPriceWithDiscount());
        entity.setDiscount(DiscountMapper.toEntity(domain.getDiscount()));
        entity.setPolicyNumber(domain.getPolicyNumber());
        entity.setCreatedByUser(UserMapper.toEntity(domain.getCreatedByUser()));
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}