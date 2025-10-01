package com.safetrip.backend.infrastructure.persistence.mapper;

import com.safetrip.backend.domain.model.Parameter;
import com.safetrip.backend.infrastructure.persistence.entity.ParameterEntity;

public class ParameterMapper {

    private ParameterMapper() {
    }

    public static Parameter toDomain(ParameterEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Parameter(
                entity.getParameterId(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static ParameterEntity toEntity(Parameter domain) {
        if (domain == null) {
            return null;
        }

        return ParameterEntity.builder()
                .parameterId(domain.getParameterId())
                .description(domain.getDescription())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}