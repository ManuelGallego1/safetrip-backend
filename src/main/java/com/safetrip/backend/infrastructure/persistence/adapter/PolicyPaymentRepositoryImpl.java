package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.model.PolicyPayment;
import com.safetrip.backend.domain.repository.PolicyPaymentRepository;
import com.safetrip.backend.infrastructure.persistence.entity.PolicyPaymentEntity;
import com.safetrip.backend.infrastructure.persistence.mapper.PolicyPaymentMapper;
import com.safetrip.backend.infrastructure.persistence.repository.PolicyPaymentJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PolicyPaymentRepositoryImpl implements PolicyPaymentRepository {

    private final PolicyPaymentJpaRepository policyPaymentJpaRepository;

    public PolicyPaymentRepositoryImpl(PolicyPaymentJpaRepository policyPaymentJpaRepository) {
        this.policyPaymentJpaRepository = policyPaymentJpaRepository;
    }

    @Override
    public PolicyPayment save(PolicyPayment policyPayment) {
        PolicyPaymentEntity entity = PolicyPaymentMapper.toEntity(policyPayment);
        return PolicyPaymentMapper.toDomain(policyPaymentJpaRepository.save(entity));
    }

    @Override
    public Optional<PolicyPayment> findById(Long policyPaymentId) {
        return policyPaymentJpaRepository.findById(policyPaymentId)
                .map(PolicyPaymentMapper::toDomain);
    }

    @Override
    public List<PolicyPayment> findAll() {
        return policyPaymentJpaRepository.findAll()
                .stream()
                .map(PolicyPaymentMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long policyPaymentId) {
        policyPaymentJpaRepository.deleteById(policyPaymentId);
    }
}