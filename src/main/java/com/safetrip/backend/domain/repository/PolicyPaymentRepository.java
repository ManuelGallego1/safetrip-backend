package com.safetrip.backend.domain.repository;

import com.safetrip.backend.domain.model.Payment;
import com.safetrip.backend.domain.model.PolicyPayment;

import java.util.List;
import java.util.Optional;

public interface PolicyPaymentRepository {

    PolicyPayment save(PolicyPayment policyPayment);

    Optional<PolicyPayment> findById(Long policyPaymentId);

    Optional<PolicyPayment> findByPayment(Payment payment);

    List<PolicyPayment> findAll();

    void deleteById(Long policyPaymentId);
}