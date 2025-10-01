package com.safetrip.backend.domain.repository;

import com.safetrip.backend.domain.model.PolicyType;

import java.util.List;
import java.util.Optional;

public interface PolicyTypeRepository {

    PolicyType save(PolicyType policyType);

    Optional<PolicyType> findById(Long id);

    List<PolicyType> findAll();

    Optional<PolicyType> findByName(String name);

    void deleteById(Long id);
}