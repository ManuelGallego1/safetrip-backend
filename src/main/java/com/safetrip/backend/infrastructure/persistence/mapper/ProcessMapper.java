package com.safetrip.backend.infrastructure.persistence.mapper;

import com.safetrip.backend.domain.model.Process;
import com.safetrip.backend.infrastructure.persistence.entity.ProcessEntity;

public class ProcessMapper {

    public static Process toDomain(ProcessEntity entity) {
        if (entity == null) return null;

        return new Process(
                entity.getProcessId(),
                ParameterMapper.toDomain(entity.getParameter()), // mapea el Parameter
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static ProcessEntity toEntity(Process domain) {
        if (domain == null) return null;

        return ProcessEntity.builder()
                .processId(domain.getProcessId())
                .parameter(ParameterMapper.toEntity(domain.getParameter()))
                .description(domain.getDescription())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}