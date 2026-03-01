package com.safetrip.backend.infrastructure.persistence.repository;

import com.safetrip.backend.infrastructure.persistence.entity.PolicyEntity;
import com.safetrip.backend.domain.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface PolicyJpaRepository extends JpaRepository<PolicyEntity, Long> {

    Optional<PolicyEntity> findByPolicyNumber(String policyNumber);

    @Query("""
        SELECT p FROM PolicyEntity p
        WHERE p.createdByUser.userId = :userId
        ORDER BY p.createdAt DESC
    """)
    Page<PolicyEntity> findByCreatedByUserUserIdOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            Pageable pageable
    );

    boolean existsByPolicyNumber(String policyNumber);

    List<PolicyEntity> findByCreatedByUserUserIdOrderByCreatedAtDesc(Long userId);

    @Modifying
    @Query("UPDATE PolicyEntity p SET " +
            "p.policyNumber = COALESCE(:policyNumber, p.policyNumber), " +
            "p.updatedAt = COALESCE(:updatedAt, p.updatedAt), " +
            "p.unitPriceWithDiscount = COALESCE(:unitPrice, p.unitPriceWithDiscount), " +
            "p.personCount = COALESCE(:personCount, p.personCount) " +
            "WHERE p.policyId = :policyId")
    int patchPolicy(
            @Param("policyId") Long policyId,
            @Param("policyNumber") String policyNumber,
            @Param("updatedAt") ZonedDateTime updatedAt,
            @Param("unitPrice") BigDecimal unitPrice,
            @Param("personCount") Integer personCount
    );

    Page<PolicyEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
        SELECT p FROM PolicyEntity p
        JOIN PolicyPaymentEntity pp ON pp.policy.policyId = p.policyId
        JOIN PaymentEntity pay ON pay.paymentId = pp.payment.paymentId
        WHERE pay.status = :status
        ORDER BY p.createdAt DESC
    """)
    Page<PolicyEntity> findAllByPaymentStatus(@Param("status") PaymentStatus status, Pageable pageable);

    Page<PolicyEntity> findAllByCreatedAtBetween(ZonedDateTime start, ZonedDateTime end, Pageable pageable);

    Page<PolicyEntity> findAllByCreatedAtAfter(ZonedDateTime start, Pageable pageable);

    Page<PolicyEntity> findAllByCreatedAtBefore(ZonedDateTime end, Pageable pageable);

    @Query("""
        SELECT p FROM PolicyEntity p
        JOIN PolicyPaymentEntity pp ON pp.policy.policyId = p.policyId
        JOIN PaymentEntity pay ON pay.paymentId = pp.payment.paymentId
        WHERE pay.status = 'COMPLETED'
        ORDER BY p.createdAt DESC
    """)
    List<PolicyEntity> findAllCompletedPolicies();

    @Query("""
        SELECT p FROM PolicyEntity p
        JOIN PolicyPaymentEntity pp ON pp.policy.policyId = p.policyId
        JOIN PaymentEntity pay ON pay.paymentId = pp.payment.paymentId
        WHERE pay.status = 'COMPLETED'
        AND p.createdAt BETWEEN :startDate AND :endDate
        ORDER BY p.createdAt DESC
    """)
    List<PolicyEntity> findCompletedPoliciesByDateRange(
            @Param("startDate") ZonedDateTime startDate,
            @Param("endDate") ZonedDateTime endDate);

    @Query("""
        SELECT p FROM PolicyEntity p
        JOIN PolicyPaymentEntity pp ON pp.policy.policyId = p.policyId
        JOIN PaymentEntity pay ON pay.paymentId = pp.payment.paymentId
        WHERE pay.status = 'COMPLETED'
        AND p.createdAt >= :startDate
        ORDER BY p.createdAt DESC
    """)
    List<PolicyEntity> findCompletedPoliciesAfter(@Param("startDate") ZonedDateTime startDate);

    @Query("""
        SELECT p FROM PolicyEntity p
        JOIN PolicyPaymentEntity pp ON pp.policy.policyId = p.policyId
        JOIN PaymentEntity pay ON pay.paymentId = pp.payment.paymentId
        WHERE pay.status = 'COMPLETED'
        AND p.createdAt <= :endDate
        ORDER BY p.createdAt DESC
    """)
    List<PolicyEntity> findCompletedPoliciesBefore(@Param("endDate") ZonedDateTime endDate);

    @Query("""
        SELECT COUNT(p) FROM PolicyEntity p
        JOIN PolicyPaymentEntity pp ON pp.policy.policyId = p.policyId
        JOIN PaymentEntity pay ON pay.paymentId = pp.payment.paymentId
        WHERE pay.status = :status
    """)
    long countByPaymentStatus(@Param("status") PaymentStatus status);

    @Query("""
        SELECT p FROM PolicyEntity p
        JOIN p.createdByUser u
        JOIN u.person per
        WHERE (:query IS NULL OR 
               LOWER(p.policyNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR
               LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR
               LOWER(per.fullName) LIKE LOWER(CONCAT('%', :query, '%')))
        AND (:userId IS NULL OR u.userId = :userId)
        AND (:policyTypeId IS NULL OR p.policyType.policyTypeId = :policyTypeId)
        ORDER BY p.createdAt DESC
    """)
    Page<PolicyEntity> searchPolicies(
            @Param("query") String query,
            @Param("userId") Long userId,
            @Param("policyTypeId") Long policyTypeId,
            Pageable pageable);
}
