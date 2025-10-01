package com.safetrip.backend.domain.repository;

import com.safetrip.backend.domain.model.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(Long paymentId);

    List<Payment> findAll();

    void deleteById(Long paymentId);

    Optional<Payment> findByTransactionId(String transactionId);
}