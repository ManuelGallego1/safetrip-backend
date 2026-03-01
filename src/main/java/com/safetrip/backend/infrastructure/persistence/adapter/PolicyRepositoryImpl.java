package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.model.Policy;
import com.safetrip.backend.domain.model.enums.PaymentStatus;
import com.safetrip.backend.domain.repository.PolicyRepository;
import com.safetrip.backend.infrastructure.persistence.entity.PolicyEntity;
import com.safetrip.backend.infrastructure.persistence.mapper.PolicyMapper;
import com.safetrip.backend.infrastructure.persistence.repository.PolicyJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
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

        List<Policy> content = entitiesPage.getContent().stream()
                .map(PolicyMapper::toDomain)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageRequest, entitiesPage.getTotalElements());
    }

    @Override
    public List<Policy> findByCreatedByUserIdOrderByCreatedAtDesc(Long userId) {
        return policyJpaRepository
                .findByCreatedByUserUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(PolicyMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public int patchPolicy(Long policyId, String policyNumber, ZonedDateTime updatedAt,
                           BigDecimal unitPrice, Integer personCount) {
        ZonedDateTime actualUpdatedAt = updatedAt != null ? updatedAt : ZonedDateTime.now();
        return policyJpaRepository.patchPolicy(
                policyId,
                policyNumber,
                actualUpdatedAt,
                unitPrice,
                personCount
        );
    }

    // ==================== MÉTODOS PARA ADMIN ====================

    @Override
    public Page<Policy> findAllOrderByCreatedAtDesc(Pageable pageable) {
        log.debug("📊 Obteniendo todas las pólizas ordenadas por fecha");

        Page<PolicyEntity> entitiesPage = policyJpaRepository.findAllByOrderByCreatedAtDesc(pageable);

        List<Policy> content = entitiesPage.getContent().stream()
                .map(PolicyMapper::toDomain)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, entitiesPage.getTotalElements());
    }

    @Override
    public Page<Policy> findAllByPaymentStatus(PaymentStatus status, Pageable pageable) {
        log.debug("📊 Obteniendo pólizas con estado de pago: {}", status);

        Page<PolicyEntity> entitiesPage = policyJpaRepository
                .findAllByPaymentStatus(status, pageable);

        List<Policy> content = entitiesPage.getContent().stream()
                .map(PolicyMapper::toDomain)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, entitiesPage.getTotalElements());
    }

    @Override
    public Page<Policy> findAllByDateRange(ZonedDateTime startDate, ZonedDateTime endDate,
                                           Pageable pageable) {
        log.debug("📊 Obteniendo pólizas entre {} y {}", startDate, endDate);

        Page<PolicyEntity> entitiesPage;

        if (startDate != null && endDate != null) {
            entitiesPage = policyJpaRepository.findAllByCreatedAtBetween(startDate, endDate, pageable);
        } else if (startDate != null) {
            entitiesPage = policyJpaRepository.findAllByCreatedAtAfter(startDate, pageable);
        } else if (endDate != null) {
            entitiesPage = policyJpaRepository.findAllByCreatedAtBefore(endDate, pageable);
        } else {
            entitiesPage = policyJpaRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        List<Policy> content = entitiesPage.getContent().stream()
                .map(PolicyMapper::toDomain)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, entitiesPage.getTotalElements());
    }

    @Override
    public List<Policy> findAllCompletedPolicies() {
        log.debug("📊 Obteniendo todas las pólizas completadas");

        return policyJpaRepository.findAllCompletedPolicies()
                .stream()
                .map(PolicyMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Policy> findCompletedPoliciesByDateRange(ZonedDateTime startDate,
                                                         ZonedDateTime endDate) {
        log.debug("📊 Obteniendo pólizas completadas entre {} y {}", startDate, endDate);

        List<PolicyEntity> entities;

        if (startDate != null && endDate != null) {
            entities = policyJpaRepository.findCompletedPoliciesByDateRange(startDate, endDate);
        } else if (startDate != null) {
            entities = policyJpaRepository.findCompletedPoliciesAfter(startDate);
        } else if (endDate != null) {
            entities = policyJpaRepository.findCompletedPoliciesBefore(endDate);
        } else {
            entities = policyJpaRepository.findAllCompletedPolicies();
        }

        return entities.stream()
                .map(PolicyMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByPaymentStatus(PaymentStatus status) {
        log.debug("📊 Contando pólizas con estado: {}", status);

        return policyJpaRepository.countByPaymentStatus(status);
    }

    @Override
    public Page<Policy> searchPolicies(String query, Long userId, Long policyTypeId,
                                       Pageable pageable) {
        log.debug("🔎 Buscando pólizas - query: {}, userId: {}, policyTypeId: {}",
                query, userId, policyTypeId);

        Page<PolicyEntity> entitiesPage = policyJpaRepository.searchPolicies(
                query, userId, policyTypeId, pageable
        );

        List<Policy> content = entitiesPage.getContent().stream()
                .map(PolicyMapper::toDomain)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, entitiesPage.getTotalElements());
    }

    // 🆕 AGREGAR ESTE MÉTODO
    @Override
    public boolean existsByPolicyNumber(String policyNumber) {
        log.debug("🔍 Verificando existencia de número de póliza: {}", policyNumber);
        return policyJpaRepository.existsByPolicyNumber(policyNumber);
    }
}