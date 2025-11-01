package com.safetrip.backend.domain.repository;

import com.safetrip.backend.domain.model.Payment;
import com.safetrip.backend.domain.model.Policy;
import com.safetrip.backend.domain.model.PolicyPayment;
import com.safetrip.backend.infrastructure.persistence.entity.PolicyEntity;

import java.util.List;
import java.util.Optional;

public interface PolicyPaymentRepository {

    PolicyPayment save(PolicyPayment policyPayment);

    Optional<PolicyPayment> findById(Long policyPaymentId);

    Optional<PolicyPayment> findByPayment(Payment payment);

    Optional<PolicyPayment> findByPolicy(Policy policy);

    List<PolicyPayment> findAll();

    void deleteById(Long policyPaymentId);
}