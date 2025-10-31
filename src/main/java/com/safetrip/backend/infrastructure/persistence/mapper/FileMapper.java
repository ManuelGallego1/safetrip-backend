package com.safetrip.backend.infrastructure.persistence.mapper;

import com.safetrip.backend.domain.model.File;
import com.safetrip.backend.infrastructure.persistence.entity.FileEntity;

public class FileMapper {

    public static File toDomain(FileEntity entity) {
        if (entity == null) return null;
        return new File(
                entity.getFileId(),
                entity.getFileName(),
                entity.getOriginalName(),
                entity.getContentType(),
                entity.getBucket(),
                entity.getSize(),
                entity.getCreatedAt()
        );
    }

    public static FileEntity toEntity(File domain) {
        if (domain == null) return null;
        return FileEntity.builder()
                .fileId(domain.getFileId())
                .fileName(domain.getFileName())
                .originalName(domain.getOriginalName())
                .contentType(domain.getContentType())
                .bucket(domain.getBucket())
                .size(domain.getSize())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}