package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.model.PolicyPerson;
import com.safetrip.backend.domain.repository.PolicyPersonRepository;
import com.safetrip.backend.infrastructure.persistence.entity.PolicyPersonEntity;
import com.safetrip.backend.infrastructure.persistence.mapper.PolicyPersonMapper;
import com.safetrip.backend.infrastructure.persistence.repository.PolicyPersonJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PolicyPersonRepositoryImpl implements PolicyPersonRepository {

    private final PolicyPersonJpaRepository policyPersonJpaRepository;

    public PolicyPersonRepositoryImpl(PolicyPersonJpaRepository policyPersonJpaRepository) {
        this.policyPersonJpaRepository = policyPersonJpaRepository;
    }

    @Override
    public PolicyPerson save(PolicyPerson policyPerson) {
        PolicyPersonEntity entity = PolicyPersonMapper.toEntity(policyPerson);
        PolicyPersonEntity saved = policyPersonJpaRepository.save(entity);
        return PolicyPersonMapper.toDomain(saved);
    }

    @Override
    public Optional<PolicyPerson> findById(Long policyPersonId) {
        return policyPersonJpaRepository.findById(policyPersonId)
                .map(PolicyPersonMapper::toDomain);
    }

    @Override
    public List<PolicyPerson> findByPolicyId(Long policyId) {
        return policyPersonJpaRepository.findByPolicy_PolicyId(policyId).stream()
                .map(PolicyPersonMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PolicyPerson> findByPersonId(Long personId) {
        return policyPersonJpaRepository.findByPerson_PersonId(personId).stream()
                .map(PolicyPersonMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PolicyPerson> findAll() {
        return policyPersonJpaRepository.findAll().stream()
                .map(PolicyPersonMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long policyPersonId) {
        policyPersonJpaRepository.deleteById(policyPersonId);
    }
}