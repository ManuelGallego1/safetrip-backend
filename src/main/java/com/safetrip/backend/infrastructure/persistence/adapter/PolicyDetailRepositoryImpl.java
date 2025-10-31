package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.model.Policy;
import com.safetrip.backend.domain.model.PolicyDetail;
import com.safetrip.backend.domain.repository.PolicyDetailRepository;
import com.safetrip.backend.infrastructure.persistence.entity.PolicyDetailEntity;
import com.safetrip.backend.infrastructure.persistence.entity.PolicyEntity;
import com.safetrip.backend.infrastructure.persistence.mapper.PolicyDetailMapper;
import com.safetrip.backend.infrastructure.persistence.mapper.PolicyMapper;
import com.safetrip.backend.infrastructure.persistence.repository.PolicyDetailJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PolicyDetailRepositoryImpl implements PolicyDetailRepository {

    private final PolicyDetailJpaRepository policyDetailJpaRepository;

    public PolicyDetailRepositoryImpl(PolicyDetailJpaRepository policyDetailJpaRepository) {
        this.policyDetailJpaRepository = policyDetailJpaRepository;
    }

    @Override
    public PolicyDetail save(PolicyDetail policyDetail) {
        PolicyDetailEntity entity = PolicyDetailMapper.toEntity(policyDetail);
        return PolicyDetailMapper.toDomain(policyDetailJpaRepository.save(entity));
    }

    @Override
    public Optional<PolicyDetail> findById(Long id) {
        return policyDetailJpaRepository.findById(id)
                .map(PolicyDetailMapper::toDomain);
    }

    @Override
    public List<PolicyDetail> findAll() {
        return policyDetailJpaRepository.findAll()
                .stream()
                .map(PolicyDetailMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        policyDetailJpaRepository.deleteById(id);
    }

    @Override
    public Optional<PolicyDetail> findByPolicyId(Policy policy) {
        return policyDetailJpaRepository.findByPolicy_PolicyId(policy.getPolicyId())
                .map(PolicyDetailMapper::toDomain);
    }
}