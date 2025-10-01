package com.safetrip.backend.infrastructure.persistence.mapper;

import com.safetrip.backend.domain.model.Role;
import com.safetrip.backend.infrastructure.persistence.entity.RoleEntity;

public class RoleMapper {

    private RoleMapper() {
        // evitar instanciación
    }

    public static Role toDomain(RoleEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Role(
                entity.getRoleId(),
                entity.getName(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static RoleEntity toEntity(Role role) {
        if (role == null) {
            return null;
        }
        return RoleEntity.builder()
                .roleId(role.getRoleId())
                .name(role.getName())
                .description(role.getDescription())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
}
