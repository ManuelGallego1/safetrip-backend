package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.model.Payment;
import com.safetrip.backend.domain.model.enums.PaymentStatus;
import com.safetrip.backend.domain.repository.PaymentRepository;
import com.safetrip.backend.infrastructure.persistence.entity.PaymentEntity;
import com.safetrip.backend.infrastructure.persistence.mapper.PaymentMapper;
import com.safetrip.backend.infrastructure.persistence.repository.PaymentJpaRepository;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    public PaymentRepositoryImpl(PaymentJpaRepository paymentJpaRepository) {
        this.paymentJpaRepository = paymentJpaRepository;
    }

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
}