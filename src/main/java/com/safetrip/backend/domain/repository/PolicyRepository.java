package com.safetrip.backend.domain.repository;

import com.safetrip.backend.domain.model.Policy;

import java.util.List;
import java.util.Optional;

public interface PolicyRepository {

    Policy save(Policy policy);

    Optional<Policy> findById(Long id);

    List<Policy> findAll();

    void deleteById(Long id);

    Optional<Policy> findByPolicyNumber(String policyNumber);
}