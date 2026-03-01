package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.model.Payment;
import com.safetrip.backend.domain.model.enums.PaymentStatus;
import com.safetrip.backend.domain.repository.PaymentRepository;
import com.safetrip.backend.infrastructure.persistence.entity.PaymentEntity;
import com.safetrip.backend.infrastructure.persistence.mapper.PaymentMapper;
import com.safetrip.backend.infrastructure.persistence.repository.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = PaymentMapper.toEntity(payment);
        return PaymentMapper.toDomain(paymentJpaRepository.save(entity));
    }

    @Override
    public Optional<Payment> findById(Long paymentId) {
        return paymentJpaRepository.findById(paymentId)
                .map(PaymentMapper::toDomain);
    }

    @Override
    public List<Payment> findAll() {
        return paymentJpaRepository.findAll()
                .stream()
                .map(PaymentMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long paymentId) {
        paymentJpaRepository.deleteById(paymentId);
    }

    @Override
    public Optional<Payment> findByTransactionId(String transactionId) {
        return paymentJpaRepository.findByTransactionId(transactionId)
                .map(PaymentMapper::toDomain);
    }

    @Override
    public int updateStatus(Long paymentId, PaymentStatus status, ZonedDateTime updatedAt) {
        return paymentJpaRepository.updatePaymentStatus(paymentId, status, updatedAt);
    }

    @Override
    public List<Payment> findByUserId(Long userId) {
        return paymentJpaRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(PaymentMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countAllCompletedPayments() {
        log.debug("📊 Contando todos los pagos completados");
        return paymentJpaRepository.countAllCompletedPayments();
    }

    @Override
    public long countCompletedPaymentsByDateRange(ZonedDateTime startDate, ZonedDateTime endDate) {
        log.debug("📊 Contando pagos completados por rango: {} - {}", startDate, endDate);

        if (startDate != null && endDate != null) {
            return paymentJpaRepository.countCompletedPaymentsBetweenDates(startDate, endDate);
        } else if (startDate != null) {
            return paymentJpaRepository.countCompletedPaymentsAfterDate(startDate);
        } else if (endDate != null) {
            return paymentJpaRepository.countCompletedPaymentsBeforeDate(endDate);
        } else {
            return paymentJpaRepository.countAllCompletedPayments();
        }
    }

    @Override
    public BigDecimal sumAllCompletedPaymentsAmount() {
        log.debug("💰 Sumando revenue de todos los pagos completados");
        return paymentJpaRepository.sumAllCompletedPaymentsAmount();
    }

    @Override
    public BigDecimal sumCompletedPaymentsAmountByDateRange(ZonedDateTime startDate, ZonedDateTime endDate) {
        log.debug("💰 Sumando revenue por rango: {} - {}", startDate, endDate);

        if (startDate != null && endDate != null) {
            return paymentJpaRepository.sumCompletedPaymentsAmountBetweenDates(startDate, endDate);
        } else if (startDate != null) {
            return paymentJpaRepository.sumCompletedPaymentsAmountAfterDate(startDate);
        } else if (endDate != null) {
            return paymentJpaRepository.sumCompletedPaymentsAmountBeforeDate(endDate);
        } else {
            return paymentJpaRepository.sumAllCompletedPaymentsAmount();
        }
    }

    @Override
    public long countByStatus(PaymentStatus status) {
        log.debug("📊 Contando pagos por estado: {}", status);
        return paymentJpaRepository.countByStatus(status);
    }

    @Override
    public BigDecimal sumAllCompletedPaymentsWithoutPolicy() {
        log.debug("💰 Sumando pagos completados sin póliza asociada (todos los tiempos)");
        BigDecimal amount = paymentJpaRepository.sumAllCompletedPaymentsWithoutPolicy();
        log.debug("✅ Monto total sin póliza: ${}", amount);
        return amount;
    }

    @Override
    public BigDecimal sumCompletedPaymentsWithoutPolicyByDateRange(ZonedDateTime startDate, ZonedDateTime endDate) {
        log.debug("💰 Sumando pagos sin póliza por rango: {} - {}", startDate, endDate);

        BigDecimal amount;

        if (startDate != null && endDate != null) {
            log.debug("📅 Usando query BETWEEN con ambas fechas");
            amount = paymentJpaRepository.sumCompletedPaymentsWithoutPolicyBetweenDates(startDate, endDate);
        } else if (startDate != null) {
            log.debug("📅 Usando query >= con solo startDate");
            amount = paymentJpaRepository.sumCompletedPaymentsWithoutPolicyAfterDate(startDate);
        } else if (endDate != null) {
            log.debug("📅 Usando query <= con solo endDate");
            amount = paymentJpaRepository.sumCompletedPaymentsWithoutPolicyBeforeDate(endDate);
        } else {
            log.debug("📅 Usando query sin filtro de fechas");
            amount = paymentJpaRepository.sumAllCompletedPaymentsWithoutPolicy();
        }

        log.debug("✅ Monto sin póliza en rango: ${}", amount);
        return amount;
    }

    // ========== IMPLEMENTACIÓN NUEVOS MÉTODOS ==========

    @Override
    public List<Payment> findCompletedPaymentsBetweenDates(ZonedDateTime startDate, ZonedDateTime endDate) {
        log.debug("📊 Obteniendo pagos completados entre {} y {}", startDate, endDate);
        return paymentJpaRepository.findCompletedPaymentsBetweenDates(startDate, endDate)
                .stream()
                .map(PaymentMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> findUserIdsByAdvisorCode(String advisorCode) {
        log.debug("🔍 Buscando usuarios que usaron código de asesor: {}", advisorCode);
        return paymentJpaRepository.findUserIdsByAdvisorCode(advisorCode);
    }

    @Override
    public List<Payment> findCompletedPaymentsByUserIdsAndDateRange(List<Long> userIds, ZonedDateTime startDate, ZonedDateTime endDate) {
        log.debug("📊 Obteniendo pagos de {} usuarios entre {} y {}", userIds.size(), startDate, endDate);

        if (userIds.isEmpty()) {
            log.warn("⚠️ Lista de userIds vacía, retornando lista vacía");
            return List.of();
        }

        return paymentJpaRepository.findCompletedPaymentsByUserIdsAndDateRange(userIds, startDate, endDate)
                .stream()
                .map(PaymentMapper::toDomain)
                .collect(Collectors.toList());
    }
}