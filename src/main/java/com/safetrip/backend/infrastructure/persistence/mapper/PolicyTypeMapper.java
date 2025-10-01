package com.safetrip.backend.infrastructure.persistence.mapper;

import com.safetrip.backend.domain.model.PolicyType;
import com.safetrip.backend.infrastructure.persistence.entity.PolicyTypeEntity;

public class PolicyTypeMapper {

    public static PolicyType toDomain(PolicyTypeEntity entity) {
        if (entity == null) return null;

        return new PolicyType(
                entity.getPolicyTypeId(),
                entity.getName(),
                entity.getBaseValue(),
                entity.getActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static PolicyTypeEntity toEntity(PolicyType domain) {
        if (domain == null) return null;

        PolicyTypeEntity entity = new PolicyTypeEntity();
        entity.setPolicyTypeId(domain.getPolicyTypeId());
        entity.setName(domain.getName());
        entity.setBaseValue(domain.getBaseValue());
        entity.setActive(domain.getActive());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
