package com.safetrip.backend.infrastructure.persistence.mapper;

import com.safetrip.backend.domain.model.PolicyFile;
import com.safetrip.backend.infrastructure.persistence.entity.PolicyFileEntity;

public class PolicyFileMapper {

    private PolicyFileMapper() {}

    public static PolicyFileEntity toEntity(PolicyFile domain) {
        return PolicyFileEntity.builder()
                .policyFileId(domain.getPolicyFileId())
                .policyId(domain.getPolicyId())
                .fileId(domain.getFileId())
                .build();
    }

    public static PolicyFile toDomain(PolicyFileEntity entity) {
        return new PolicyFile(
                entity.getPolicyFileId(),
                entity.getPolicyId(),
                entity.getFileId()
        );
    }
}
