package com.safetrip.backend.domain.repository;

import com.safetrip.backend.domain.model.PolicyDetail;

import java.util.List;
import java.util.Optional;

public interface PolicyDetailRepository {

    PolicyDetail save(PolicyDetail policyDetail);

    Optional<PolicyDetail> findById(Long id);

    List<PolicyDetail> findAll();

    void deleteById(Long id);

    List<PolicyDetail> findByPolicyId(Long policyId);
}