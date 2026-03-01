package com.safetrip.backend.infrastructure.persistence.mapper;

import com.safetrip.backend.domain.model.User;
import com.safetrip.backend.infrastructure.persistence.entity.PersonEntity;
import com.safetrip.backend.infrastructure.persistence.entity.RoleEntity;
import com.safetrip.backend.infrastructure.persistence.entity.UserEntity;

public class UserMapper {

    private UserMapper() {
        // Private constructor para evitar instanciación
    }

    public static User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        return new User(
                entity.getUserId(),
                PersonMapper.toDomain(entity.getPerson()),  // ✅ Usar PersonMapper
                entity.getEmail(),
                entity.getPhone(),
                entity.getPasswordHash(),
                entity.getRole().toDomain(),
                entity.getIsActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getProfileImageUrl()
        );
    }

    public static UserEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }

        return UserEntity.builder()
                .userId(domain.getUserId())  // ✅ Mantiene el ID
                .person(PersonMapper.toEntity(domain.getPerson()))  // ✅ CAMBIO CRÍTICO: Usar PersonMapper
                .email(domain.getEmail())
                .phone(domain.getPhone())
                .passwordHash(domain.getPasswordHash())
                .role(RoleEntity.fromDomain(domain.getRole()))
                .isActive(domain.getIsActive())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .profileImageUrl(domain.getProfileImageUrl())
                .build();
    }
}