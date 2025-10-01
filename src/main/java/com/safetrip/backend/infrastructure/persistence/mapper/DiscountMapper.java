package com.safetrip.backend.infrastructure.persistence.mapper;

import com.safetrip.backend.domain.model.Discount;
import com.safetrip.backend.infrastructure.persistence.entity.DiscountEntity;

public class DiscountMapper {

    private DiscountMapper() {
    }

    public static Discount toDomain(DiscountEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Discount(
                entity.getDiscountId(),
                entity.getName(),
                entity.getType(),
                entity.getValue(),
                entity.getActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static DiscountEntity toEntity(Discount domain) {
        if (domain == null) {
            return null;
        }

        return DiscountEntity.builder()
                .discountId(domain.getDiscountId())
                .name(domain.getName())
                .type(domain.getType())
                .value(domain.getValue())
                .active(domain.getActive())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}