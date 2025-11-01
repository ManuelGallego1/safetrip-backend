package com.safetrip.backend.infrastructure.persistence.repository;

import com.safetrip.backend.infrastructure.persistence.entity.PaymentEntity;
import com.safetrip.backend.infrastructure.persistence.entity.PersonEntity;
import com.safetrip.backend.infrastructure.persistence.entity.PolicyEntity;
import com.safetrip.backend.infrastructure.persistence.entity.PolicyPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PolicyPaymentJpaRepository extends JpaRepository<PolicyPaymentEntity, Long> {
    // Cambiar de PolicyPayment a PolicyPaymentEntity
    Optional<PolicyPaymentEntity> findByPayment(PaymentEntity payment);

    Optional<PolicyPaymentEntity> findByPolicy(PolicyEntity policy);
}