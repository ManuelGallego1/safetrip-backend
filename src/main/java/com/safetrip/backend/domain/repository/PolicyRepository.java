package com.safetrip.backend.domain.repository;

import com.safetrip.backend.domain.model.Policy;
import com.safetrip.backend.domain.model.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface PolicyRepository {

    Policy save(Policy policy);

    Optional<Policy> findById(Long id);

    List<Policy> findAll();

    void deleteById(Long id);

    Optional<Policy> findByPolicyNumber(String policyNumber);

    Page<Policy> findByCreatedByUserIdOrderByCreatedAtDesc(Long userId, PageRequest pageRequest);

    List<Policy> findByCreatedByUserIdOrderByCreatedAtDesc(Long userId);

    int patchPolicy(Long policyId, String policyNumber, ZonedDateTime updatedAt, BigDecimal unitPrice, Integer personCount);

    Page<Policy> findAllOrderByCreatedAtDesc(Pageable pageable);

    Page<Policy> findAllByPaymentStatus(PaymentStatus status, Pageable pageable);

    Page<Policy> findAllByDateRange(ZonedDateTime startDate, ZonedDateTime endDate, Pageable pageable);

    List<Policy> findAllCompletedPolicies();

    List<Policy> findCompletedPoliciesByDateRange(ZonedDateTime startDate, ZonedDateTime endDate);

    long countByPaymentStatus(PaymentStatus status);

    Page<Policy> searchPolicies(String query, Long userId, Long policyTypeId, Pageable pageable);

    boolean existsByPolicyNumber(String policyNumber);
}