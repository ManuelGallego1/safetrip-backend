package com.safetrip.backend.infrastructure.persistence.repository;

import com.safetrip.backend.domain.model.enums.PaymentStatus;
import com.safetrip.backend.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.Optional;

@Repository
public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByTransactionId(String transactionId);

    @Modifying
    @Query("UPDATE PaymentEntity p SET p.status = :status, p.updatedAt = :updatedAt WHERE p.paymentId = :paymentId")
    int updatePaymentStatus(
            @Param("paymentId") Long paymentId,
            @Param("status") PaymentStatus status,
            @Param("updatedAt") ZonedDateTime updatedAt
    );
}