package com.safetrip.backend.domain.repository;

import com.safetrip.backend.domain.model.Payment;
import com.safetrip.backend.domain.model.enums.PaymentStatus;

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
}