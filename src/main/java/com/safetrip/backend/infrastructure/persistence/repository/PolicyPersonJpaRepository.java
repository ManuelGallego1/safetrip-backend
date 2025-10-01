package com.safetrip.backend.infrastructure.persistence.repository;

import com.safetrip.backend.infrastructure.persistence.entity.PolicyPersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PolicyPersonJpaRepository extends JpaRepository<PolicyPersonEntity, Long> {

    List<PolicyPersonEntity> findByPolicy_PolicyId(Long policyId);

    List<PolicyPersonEntity> findByPerson_PersonId(Long personId);
}