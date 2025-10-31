package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.model.PolicyFile;
import com.safetrip.backend.domain.repository.PolicyFileRepository;
import com.safetrip.backend.infrastructure.persistence.entity.PolicyFileEntity;
import com.safetrip.backend.infrastructure.persistence.mapper.PolicyFileMapper;
import com.safetrip.backend.infrastructure.persistence.repository.PolicyFileJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PolicyFileRepositoryImpl implements PolicyFileRepository {

    private final PolicyFileJpaRepository policyFileJpaRepository;

    public PolicyFileRepositoryImpl(PolicyFileJpaRepository policyFileJpaRepository) {
        this.policyFileJpaRepository = policyFileJpaRepository;
    }

    @Override
    public PolicyFile save(PolicyFile policyFile) {
        PolicyFileEntity entity = PolicyFileMapper.toEntity(policyFile);
        PolicyFileEntity saved = policyFileJpaRepository.save(entity);
        return PolicyFileMapper.toDomain(saved);
    }

    @Override
    public Optional<PolicyFile> findById(Long policyFileId) {
        return policyFileJpaRepository.findById(policyFileId)
                .map(PolicyFileMapper::toDomain);
    }

    @Override
    public List<PolicyFile> findAll() {
        return policyFileJpaRepository.findAll().stream()
                .map(PolicyFileMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long policyFileId) {
        policyFileJpaRepository.deleteById(policyFileId);
    }
}