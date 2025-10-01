package com.safetrip.backend.domain.repository;

import com.safetrip.backend.domain.model.PaymentType;

import java.util.List;
import java.util.Optional;

public interface PaymentTypeRepository {

    PaymentType save(PaymentType paymentType);

    Optional<PaymentType> findById(Long paymentTypeId);

    List<PaymentType> findAll();

    void deleteById(Long paymentTypeId);

    Optional<PaymentType> findByName(String name);
}