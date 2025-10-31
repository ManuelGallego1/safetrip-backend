package com.safetrip.backend.infrastructure.persistence.repository;

import com.safetrip.backend.infrastructure.persistence.entity.PolicyFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyFileJpaRepository extends JpaRepository<PolicyFileEntity, Long> {

    List<PolicyFileEntity> findByPolicyId(Long policyId);

    List<PolicyFileEntity> findByFileId(Long fileId);

    void deleteByPolicyId(Long policyId);

    void deleteByFileId(Long fileId);
}