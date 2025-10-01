package com.safetrip.backend.infrastructure.persistence.mapper;

import com.safetrip.backend.domain.model.PolicyPerson;
import com.safetrip.backend.infrastructure.persistence.entity.PolicyPersonEntity;

public class PolicyPersonMapper {

    private PolicyPersonMapper() {
    }

    public static PolicyPerson toDomain(PolicyPersonEntity entity) {
        if (entity == null) {
            return null;
        }

        return new PolicyPerson(
                entity.getPolicyPersonId(),
                PolicyMapper.toDomain(entity.getPolicy()),
                PersonMapper.toDomain(entity.getPerson()),
                entity.getRelationship(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static PolicyPersonEntity toEntity(PolicyPerson domain) {
        if (domain == null) {
            return null;
        }

        return PolicyPersonEntity.builder()
                .policyPersonId(domain.getPolicyPersonId())
                .policy(PolicyMapper.toEntity(domain.getPolicy()))
                .person(PersonMapper.toEntity(domain.getPerson()))
                .relationship(domain.getRelationship())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}