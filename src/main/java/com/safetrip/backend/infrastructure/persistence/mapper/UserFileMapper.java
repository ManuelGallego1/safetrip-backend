package com.safetrip.backend.infrastructure.persistence.mapper;

import com.safetrip.backend.domain.model.UserFile;
import com.safetrip.backend.infrastructure.persistence.entity.UserFileEntity;

public class UserFileMapper {

    public static UserFile toDomain(UserFileEntity entity) {
        if (entity == null) return null;
        return new UserFile(entity.getUserFileId(), entity.getUserId(), entity.getFileId());
    }

    public static UserFileEntity toEntity(UserFile domain) {
        if (domain == null) return null;
        return UserFileEntity.builder()
                .userFileId(domain.getUserFileId())
                .userId(domain.getUserId())
                .fileId(domain.getFileId())
                .build();
    }
}