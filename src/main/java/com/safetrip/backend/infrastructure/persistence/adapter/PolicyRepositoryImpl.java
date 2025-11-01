package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.model.Policy;
import com.safetrip.backend.domain.repository.PolicyRepository;
import com.safetrip.backend.infrastructure.persistence.entity.PolicyEntity;
import com.safetrip.backend.infrastructure.persistence.mapper.PolicyMapper;
import com.safetrip.backend.infrastructure.persistence.repository.PolicyJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class PolicyRepositoryImpl implements PolicyRepository {

    private final PolicyJpaRepository policyJpaRepository;

    @Override
    public Policy save(Policy policy) {
        PolicyEntity entity = PolicyMapper.toEntity(policy);
        PolicyEntity savedEntity = policyJpaRepository.save(entity);
        return PolicyMapper.toDomain(savedEntity);
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

    @Override
    public Page<Policy> findByCreatedByUserIdOrderByCreatedAtDesc(Long userId, PageRequest pageRequest) {
        Page<PolicyEntity> entitiesPage = policyJpaRepository
                .findByCreatedByUserUserIdOrderByCreatedAtDesc(userId, pageRequest);

        // Mapear todo, incluso nulos (no filtrar)
        List<Policy> content = entitiesPage.getContent().stream()
                .map(PolicyMapper::toDomain)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageRequest, entitiesPage.getTotalElements());
    }

    @Override
    public List<Policy> findByCreatedByUserIdOrderByCreatedAtDesc(Long userId) {
        List<PolicyEntity> entitiesList = policyJpaRepository
                .findByCreatedByUserUserIdOrderByCreatedAtDesc(userId);

        return entitiesList.stream()
                .map(PolicyMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public int patchPolicy(Long policyId, String policyNumber, ZonedDateTime updatedAt, BigDecimal unitPrice, Integer personCount) {
        ZonedDateTime actualUpdatedAt = updatedAt != null ? updatedAt : ZonedDateTime.now();

        return policyJpaRepository.patchPolicy(
                policyId,
                policyNumber,
                actualUpdatedAt,
                unitPrice,
                personCount
        );
    }

}