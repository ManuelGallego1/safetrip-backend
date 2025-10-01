package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.model.Policy;
import com.safetrip.backend.domain.repository.PolicyRepository;
import com.safetrip.backend.infrastructure.persistence.entity.PolicyEntity;
import com.safetrip.backend.infrastructure.persistence.mapper.PolicyMapper;
import com.safetrip.backend.infrastructure.persistence.repository.PolicyJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PolicyRepositoryImpl implements PolicyRepository {

    private final PolicyJpaRepository policyJpaRepository;

    public PolicyRepositoryImpl(PolicyJpaRepository policyJpaRepository) {
        this.policyJpaRepository = policyJpaRepository;
    }

    @Override
    public Policy save(Policy policy) {
        PolicyEntity entity = PolicyMapper.toEntity(policy);
        return PolicyMapper.toDomain(policyJpaRepository.save(entity));
    }

    @Override
    public Optional<Policy> findById(Long id) {
        return policyJpaRepository.findById(id)
                .map(PolicyMapper::toDomain);
    }

    @Override
    public List<Policy> findAll() {
        return policyJpaRepository.findAll()
                .stream()
                .map(PolicyMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        policyJpaRepository.deleteById(id);
    }

    @Override
    public Optional<Policy> findByPolicyNumber(String policyNumber) {
        return policyJpaRepository.findByPolicyNumber(policyNumber)
                .map(PolicyMapper::toDomain);
    }
}