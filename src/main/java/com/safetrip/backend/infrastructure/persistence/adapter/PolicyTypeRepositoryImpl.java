package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.model.PolicyType;
import com.safetrip.backend.domain.repository.PolicyTypeRepository;
import com.safetrip.backend.infrastructure.persistence.entity.PolicyTypeEntity;
import com.safetrip.backend.infrastructure.persistence.mapper.PolicyTypeMapper;
import com.safetrip.backend.infrastructure.persistence.repository.PolicyTypeJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PolicyTypeRepositoryImpl implements PolicyTypeRepository {

    private final PolicyTypeJpaRepository policyTypeJpaRepository;

    public PolicyTypeRepositoryImpl(PolicyTypeJpaRepository policyTypeJpaRepository) {
        this.policyTypeJpaRepository = policyTypeJpaRepository;
    }

    @Override
    public PolicyType save(PolicyType policyType) {
        PolicyTypeEntity entity = PolicyTypeMapper.toEntity(policyType);
        return PolicyTypeMapper.toDomain(policyTypeJpaRepository.save(entity));
    }

    @Override
    public Optional<PolicyType> findById(Long id) {
        return policyTypeJpaRepository.findById(id)
                .map(PolicyTypeMapper::toDomain);
    }

    @Override
    public List<PolicyType> findAll() {
        return policyTypeJpaRepository.findAll()
                .stream()
                .map(PolicyTypeMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        policyTypeJpaRepository.deleteById(id);
    }
}