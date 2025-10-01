package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.model.PaymentType;
import com.safetrip.backend.domain.repository.PaymentTypeRepository;
import com.safetrip.backend.infrastructure.persistence.entity.PaymentTypeEntity;
import com.safetrip.backend.infrastructure.persistence.mapper.PaymentTypeMapper;
import com.safetrip.backend.infrastructure.persistence.repository.PaymentTypeJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PaymentTypeRepositoryImpl implements PaymentTypeRepository {

    private final PaymentTypeJpaRepository paymentTypeJpaRepository;

    public PaymentTypeRepositoryImpl(PaymentTypeJpaRepository paymentTypeJpaRepository) {
        this.paymentTypeJpaRepository = paymentTypeJpaRepository;
    }

    @Override
    public PaymentType save(PaymentType paymentType) {
        PaymentTypeEntity entity = PaymentTypeMapper.toEntity(paymentType);
        return PaymentTypeMapper.toDomain(paymentTypeJpaRepository.save(entity));
    }

    @Override
    public Optional<PaymentType> findById(Long paymentTypeId) {
        return paymentTypeJpaRepository.findById(paymentTypeId)
                .map(PaymentTypeMapper::toDomain);
    }

    @Override
    public List<PaymentType> findAll() {
        return paymentTypeJpaRepository.findAll()
                .stream()
                .map(PaymentTypeMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long paymentTypeId) {
        paymentTypeJpaRepository.deleteById(paymentTypeId);
    }

    @Override
    public Optional<PaymentType> findByName(String name) {
        return paymentTypeJpaRepository.findByName(name)
                .map(PaymentTypeMapper::toDomain);
    }
}