package com.safetrip.backend.infrastructure.persistence.mapper;

import com.safetrip.backend.domain.model.Otp;
import com.safetrip.backend.infrastructure.persistence.entity.OtpEntity;

public class OtpMapper {

    private OtpMapper() {
    }

    public static Otp toDomain(OtpEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Otp(
                entity.getOtpId(),
                UserMapper.toDomain(entity.getUser()),
                entity.getCode(),
                entity.getExpiration(),
                entity.getVerified(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static OtpEntity toEntity(Otp domain) {
        if (domain == null) {
            return null;
        }

        return OtpEntity.builder()
                .otpId(domain.getOtpId())
                .user(UserMapper.toEntity(domain.getUser()))
                .code(domain.getCode())
                .expiration(domain.getExpiration())
                .verified(domain.isVerified())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}