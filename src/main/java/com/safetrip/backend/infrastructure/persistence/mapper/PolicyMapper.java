package com.safetrip.backend.infrastructure.persistence.mapper;

import com.safetrip.backend.domain.model.Policy;
import com.safetrip.backend.infrastructure.persistence.entity.PolicyEntity;

import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Mapper responsible for converting between Policy domain models and Policy JPA entities.
 */
public final class PolicyMapper {

    private PolicyMapper() {
        // Prevent instantiation
    }

    // ------------------------------------------------------------
    // DOMAIN → ENTITY
    // ------------------------------------------------------------

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
        entity.setCreatedWithFile(domain.getCreatedWithFile());

        if (domain.getPolicyDetails() != null && !domain.getPolicyDetails().isEmpty()) {
            entity.setPolicyDetails(
                    domain.getPolicyDetails().stream()
                            .map(PolicyDetailMapper::toEntity)
                            .collect(Collectors.toList())
            );
        }

        if (domain.getPolicyPayments() != null && !domain.getPolicyPayments().isEmpty()) {
            entity.setPolicyPayments(
                    domain.getPolicyPayments().stream()
                            .map(PolicyPaymentMapper::toEntity)
                            .collect(Collectors.toList())
            );
        }

        return entity;
    }

    // ------------------------------------------------------------
    // ENTITY → DOMAIN
    // ------------------------------------------------------------

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
                entity.getCreatedWithFile(),
                // This prevents StackOverflowError from bidirectional relationships
                Collections.emptyList(),  // policyDetails
                Collections.emptyList()   // policyPayments
        );
    }
}