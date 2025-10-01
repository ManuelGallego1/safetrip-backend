package com.safetrip.backend.infrastructure.persistence.repository;

import com.safetrip.backend.infrastructure.persistence.entity.PolicyDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyDetailJpaRepository extends JpaRepository<PolicyDetailEntity, Long> {

    List<PolicyDetailEntity> findByPolicy_PolicyId(Long policyId);
}