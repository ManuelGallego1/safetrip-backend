package com.safetrip.backend.infrastructure.persistence.mapper;

import com.safetrip.backend.domain.model.PolicyDetail;
import com.safetrip.backend.infrastructure.persistence.entity.PolicyDetailEntity;

public class PolicyDetailMapper {

    public static PolicyDetail toDomain(PolicyDetailEntity entity) {
        if (entity == null) return null;

        return new PolicyDetail(
                entity.getPolicyDetailId(),
                PolicyMapper.toDomain(entity.getPolicy()),
                entity.getOrigin(),
                entity.getDestination(),
                entity.getDeparture(),
                entity.getArrival(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static PolicyDetailEntity toEntity(PolicyDetail domain) {
        if (domain == null) return null;

        PolicyDetailEntity entity = new PolicyDetailEntity();
        entity.setPolicyDetailId(domain.getPolicyDetailId());
        entity.setPolicy(PolicyMapper.toEntity(domain.getPolicy()));
        entity.setOrigin(domain.getOrigin());
        entity.setDestination(domain.getDestination());
        entity.setDeparture(domain.getDeparture());
        entity.setArrival(domain.getArrival());
        entity.setNotes(domain.getNotes());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        return entity;
    }
}