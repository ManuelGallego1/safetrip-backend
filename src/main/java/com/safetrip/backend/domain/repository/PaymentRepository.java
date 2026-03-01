package com.safetrip.backend.domain.repository;

import com.safetrip.backend.domain.model.Payment;
import com.safetrip.backend.domain.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);
    Optional<Payment> findById(Long paymentId);
    List<Payment> findAll();
    void deleteById(Long paymentId);
    Optional<Payment> findByTransactionId(String transactionId);
    int updateStatus(Long paymentId, PaymentStatus status, ZonedDateTime updatedAt);
    List<Payment> findByUserId(Long userId);
    long countAllCompletedPayments();
    long countCompletedPaymentsByDateRange(ZonedDateTime startDate, ZonedDateTime endDate);
    BigDecimal sumAllCompletedPaymentsAmount();
    BigDecimal sumCompletedPaymentsAmountByDateRange(ZonedDateTime startDate, ZonedDateTime endDate);
    long countByStatus(PaymentStatus status);
    BigDecimal sumAllCompletedPaymentsWithoutPolicy();
    BigDecimal sumCompletedPaymentsWithoutPolicyByDateRange(ZonedDateTime startDate, ZonedDateTime endDate);
    List<Payment> findCompletedPaymentsBetweenDates(ZonedDateTime startDate, ZonedDateTime endDate);
    List<Long> findUserIdsByAdvisorCode(String advisorCode);
    List<Payment> findCompletedPaymentsByUserIdsAndDateRange(List<Long> userIds, ZonedDateTime startDate, ZonedDateTime endDate);
}