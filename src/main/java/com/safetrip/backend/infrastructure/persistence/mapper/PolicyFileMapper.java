package com.safetrip.backend.infrastructure.persistence.mapper;

import com.safetrip.backend.domain.model.PolicyFile;
import com.safetrip.backend.infrastructure.persistence.entity.PolicyFileEntity;

public class PolicyFileMapper {

    public static PolicyFile toDomain(PolicyFileEntity entity) {
        if (entity == null) return null;
        return new PolicyFile(entity.getPolicyFileId(), entity.getPolicyId(), entity.getFileId());
    }

    public static PolicyFileEntity toEntity(PolicyFile domain) {
        if (domain == null) return null;
        return PolicyFileEntity.builder()
                .policyFileId(domain.getPolicyFileId())
                .policyId(domain.getPolicyId())
                .fileId(domain.getFileId())
                .build();
    }
}