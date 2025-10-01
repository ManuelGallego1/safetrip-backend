package com.safetrip.backend.infrastructure.persistence.repository;

import com.safetrip.backend.domain.model.PolicyType;
import com.safetrip.backend.infrastructure.persistence.entity.PolicyTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PolicyTypeJpaRepository extends JpaRepository<PolicyTypeEntity, Long> {
    Optional<PolicyType> findByName(String name);
}