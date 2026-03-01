package com.safetrip.backend.infrastructure.persistence.mapper;

import com.safetrip.backend.domain.model.PolicyPlan;
import com.safetrip.backend.infrastructure.persistence.entity.PolicyPlanEntity;

/**
 * Mapper responsible for converting between PolicyPlan domain models and PolicyPlan JPA entities.
 */
public final class PolicyPlanMapper {

    private PolicyPlanMapper() {
        // Prevent instantiation
    }

    // ------------------------------------------------------------
    // DOMAIN → ENTITY
    // ------------------------------------------------------------

    public static PolicyPlanEntity toEntity(PolicyPlan domain) {
        if (domain == null) return null;

        PolicyPlanEntity entity = new PolicyPlanEntity();
        entity.setPolicyPlanId(domain.getPolicyPlanId());
        entity.setPolicyType(PolicyTypeMapper.toEntity(domain.getPolicyType()));
        entity.setPax(domain.getPax());
        entity.setDiscountPercentage(domain.getDiscountPercentage());
        entity.setDescription(domain.getDescription());
        entity.setPopular(domain.getPopular());
        entity.setActive(domain.getActive());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        return entity;
    }

    // ------------------------------------------------------------
    // ENTITY → DOMAIN
    // ------------------------------------------------------------

    public static PolicyPlan toDomain(PolicyPlanEntity entity) {
        if (entity == null) return null;

        return new PolicyPlan(
                entity.getPolicyPlanId(),
                PolicyTypeMapper.toDomain(entity.getPolicyType()),
                entity.getPax(),
                entity.getDiscountPercentage(),
                entity.getDescription(),
                entity.getPopular(),
                entity.getActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}