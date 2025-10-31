package com.safetrip.backend.infrastructure.persistence.repository;

import com.safetrip.backend.infrastructure.persistence.entity.PolicyDetailEntity;
import com.safetrip.backend.infrastructure.persistence.entity.PolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PolicyDetailJpaRepository extends JpaRepository<PolicyDetailEntity, Long> {
    Optional<PolicyDetailEntity> findByPolicy_PolicyId(Long policyId);
}