package com.safetrip.backend.domain.repository;

import com.safetrip.backend.domain.model.PolicyPayment;

import java.util.List;
import java.util.Optional;

public interface PolicyPaymentRepository {

    PolicyPayment save(PolicyPayment policyPayment);

    Optional<PolicyPayment> findById(Long policyPaymentId);

    List<PolicyPayment> findAll();

    void deleteById(Long policyPaymentId);
}