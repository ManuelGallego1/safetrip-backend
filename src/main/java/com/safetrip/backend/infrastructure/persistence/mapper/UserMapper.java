package com.safetrip.backend.infrastructure.persistence.mapper;

import com.safetrip.backend.domain.model.User;
import com.safetrip.backend.infrastructure.persistence.entity.PersonEntity;
import com.safetrip.backend.infrastructure.persistence.entity.RoleEntity;
import com.safetrip.backend.infrastructure.persistence.entity.UserEntity;


public class UserMapper {
    public static User toDomain(UserEntity entity) {
        return new User(
                entity.getUserId(),
                entity.getPerson().toDomain(),
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
        return UserEntity.builder()
                .userId(domain.getUserId())
                .person(PersonEntity.fromDomain(domain.getPerson()))
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