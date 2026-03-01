package com.safetrip.backend.infrastructure.persistence.repository;

import com.safetrip.backend.domain.model.enums.PaymentStatus;
import com.safetrip.backend.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
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

    @Query("SELECT p FROM PaymentEntity p WHERE p.user.userId = :userId ORDER BY p.createdAt DESC")
    List<PaymentEntity> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    /**
     * ✅ Cuenta todos los pagos completados (excluyendo transacciones WALLET)
     */
    @Query("""
        SELECT COUNT(p) 
        FROM PaymentEntity p 
        WHERE p.status = 'COMPLETED' 
        AND p.transactionId NOT LIKE 'WALLET%'
        """)
    long countAllCompletedPayments();

    /**
     * ✅ Cuenta pagos completados entre dos fechas (excluyendo transacciones WALLET)
     */
    @Query("""
        SELECT COUNT(p) 
        FROM PaymentEntity p 
        WHERE p.status = 'COMPLETED' 
        AND p.transactionId NOT LIKE 'WALLET%'
        AND p.createdAt BETWEEN :startDate AND :endDate
        """)
    long countCompletedPaymentsBetweenDates(
            @Param("startDate") ZonedDateTime startDate,
            @Param("endDate") ZonedDateTime endDate);

    /**
     * ✅ Cuenta pagos completados después de una fecha (excluyendo transacciones WALLET)
     */
    @Query("""
        SELECT COUNT(p) 
        FROM PaymentEntity p 
        WHERE p.status = 'COMPLETED' 
        AND p.transactionId NOT LIKE 'WALLET%'
        AND p.createdAt >= :startDate
        """)
    long countCompletedPaymentsAfterDate(@Param("startDate") ZonedDateTime startDate);

    /**
     * ✅ Cuenta pagos completados antes de una fecha (excluyendo transacciones WALLET)
     */
    @Query("""
        SELECT COUNT(p) 
        FROM PaymentEntity p 
        WHERE p.status = 'COMPLETED' 
        AND p.transactionId NOT LIKE 'WALLET%'
        AND p.createdAt <= :endDate
        """)
    long countCompletedPaymentsBeforeDate(@Param("endDate") ZonedDateTime endDate);

    /**
     * ✅ Suma el monto total de todos los pagos completados (excluyendo transacciones WALLET)
     */
    @Query("""
        SELECT COALESCE(SUM(p.amount), 0) 
        FROM PaymentEntity p 
        WHERE p.status = 'COMPLETED' 
        AND p.transactionId NOT LIKE 'WALLET%'
        """)
    BigDecimal sumAllCompletedPaymentsAmount();

    /**
     * ✅ Suma el monto entre dos fechas (excluyendo transacciones WALLET)
     */
    @Query("""
        SELECT COALESCE(SUM(p.amount), 0) 
        FROM PaymentEntity p 
        WHERE p.status = 'COMPLETED' 
        AND p.transactionId NOT LIKE 'WALLET%'
        AND p.createdAt BETWEEN :startDate AND :endDate
        """)
    BigDecimal sumCompletedPaymentsAmountBetweenDates(
            @Param("startDate") ZonedDateTime startDate,
            @Param("endDate") ZonedDateTime endDate);

    /**
     * ✅ Suma el monto después de una fecha (excluyendo transacciones WALLET)
     */
    @Query("""
        SELECT COALESCE(SUM(p.amount), 0) 
        FROM PaymentEntity p 
        WHERE p.status = 'COMPLETED' 
        AND p.transactionId NOT LIKE 'WALLET%'
        AND p.createdAt >= :startDate
        """)
    BigDecimal sumCompletedPaymentsAmountAfterDate(@Param("startDate") ZonedDateTime startDate);

    /**
     * ✅ Suma el monto antes de una fecha (excluyendo transacciones WALLET)
     */
    @Query("""
        SELECT COALESCE(SUM(p.amount), 0) 
        FROM PaymentEntity p 
        WHERE p.status = 'COMPLETED' 
        AND p.transactionId NOT LIKE 'WALLET%'
        AND p.createdAt <= :endDate
        """)
    BigDecimal sumCompletedPaymentsAmountBeforeDate(@Param("endDate") ZonedDateTime endDate);

    @Query("""
        SELECT COUNT(p) 
        FROM PaymentEntity p 
        WHERE p.status = :status 
        AND p.transactionId NOT LIKE 'WALLET%'
        """)
    long countByStatus(@Param("status") PaymentStatus status);

    // Sin filtro de fechas
    @Query(value = """
    SELECT SUM(wt.balance)
    FROM wallet_transactions wt
    INNER JOIN LATERAL (
        SELECT wt2.wallet_transaction_id
        FROM wallet_transactions wt2
        WHERE wt2.wallet_fk = wt.wallet_fk
        ORDER BY wt2.created_at DESC
        LIMIT 1
    ) ultimo ON ultimo.wallet_transaction_id = wt.wallet_transaction_id
    """, nativeQuery = true)
    BigDecimal sumAllCompletedPaymentsWithoutPolicy();

    // Entre fechas
    @Query(value = """
    SELECT SUM(wt.balance)
    FROM wallet_transactions wt
    INNER JOIN LATERAL (
        SELECT wt2.wallet_transaction_id
        FROM wallet_transactions wt2
        WHERE wt2.wallet_fk = wt.wallet_fk
        ORDER BY wt2.created_at DESC
        LIMIT 1
    ) ultimo ON ultimo.wallet_transaction_id = wt.wallet_transaction_id
    WHERE wt.created_at BETWEEN :startDate AND :endDate
    """, nativeQuery = true)
    BigDecimal sumCompletedPaymentsWithoutPolicyBetweenDates(
            @Param("startDate") ZonedDateTime startDate,
            @Param("endDate") ZonedDateTime endDate);

    // Después de fecha
    @Query(value = """
    SELECT SUM(wt.balance)
    FROM wallet_transactions wt
    INNER JOIN LATERAL (
        SELECT wt2.wallet_transaction_id
        FROM wallet_transactions wt2
        WHERE wt2.wallet_fk = wt.wallet_fk
        ORDER BY wt2.created_at DESC
        LIMIT 1
    ) ultimo ON ultimo.wallet_transaction_id = wt.wallet_transaction_id
    WHERE wt.created_at >= :startDate
    """, nativeQuery = true)
    BigDecimal sumCompletedPaymentsWithoutPolicyAfterDate(
            @Param("startDate") ZonedDateTime startDate);

    // Antes de fecha
    @Query(value = """
    SELECT SUM(wt.balance)
    FROM wallet_transactions wt
    INNER JOIN LATERAL (
        SELECT wt2.wallet_transaction_id
        FROM wallet_transactions wt2
        WHERE wt2.wallet_fk = wt.wallet_fk
        ORDER BY wt2.created_at DESC
        LIMIT 1
    ) ultimo ON ultimo.wallet_transaction_id = wt.wallet_transaction_id
    WHERE wt.created_at <= :endDate
    """, nativeQuery = true)
    BigDecimal sumCompletedPaymentsWithoutPolicyBeforeDate(
            @Param("endDate") ZonedDateTime endDate);

    /**
     * ✅ Obtiene todos los pagos completados en un rango de fechas (para reporte general)
     */
    @Query("""
        SELECT p FROM PaymentEntity p
        JOIN FETCH p.user u
        JOIN FETCH u.person
        WHERE p.status = 'COMPLETED'
        AND p.transactionId NOT LIKE 'WALLET%'
        AND p.createdAt BETWEEN :startDate AND :endDate
        ORDER BY p.createdAt DESC
        """)
    List<PaymentEntity> findCompletedPaymentsBetweenDates(
            @Param("startDate") ZonedDateTime startDate,
            @Param("endDate") ZonedDateTime endDate);

    /**
     * ✅ Obtiene IDs de usuarios que alguna vez usaron un código de descuento específico
     */
    @Query("""
        SELECT DISTINCT pol.createdByUser.userId
        FROM PolicyEntity pol
        WHERE pol.discount.name = :advisorCode
        """)
    List<Long> findUserIdsByAdvisorCode(@Param("advisorCode") String advisorCode);

    /**
     * ✅ Obtiene pagos completados de usuarios específicos en un rango de fechas
     */
    @Query("""
        SELECT p FROM PaymentEntity p
        JOIN FETCH p.user u
        JOIN FETCH u.person
        WHERE p.status = 'COMPLETED'
        AND p.transactionId NOT LIKE 'WALLET%'
        AND p.user.userId IN :userIds
        AND p.createdAt BETWEEN :startDate AND :endDate
        ORDER BY p.createdAt DESC
        """)
    List<PaymentEntity> findCompletedPaymentsByUserIdsAndDateRange(
            @Param("userIds") List<Long> userIds,
            @Param("startDate") ZonedDateTime startDate,
            @Param("endDate") ZonedDateTime endDate);
}