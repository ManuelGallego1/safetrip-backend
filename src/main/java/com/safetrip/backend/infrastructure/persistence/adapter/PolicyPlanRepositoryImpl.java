package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.model.PolicyPlan;
import com.safetrip.backend.domain.repository.PolicyPlanRepository;
import com.safetrip.backend.infrastructure.persistence.entity.PolicyPlanEntity;
import com.safetrip.backend.infrastructure.persistence.mapper.PolicyPlanMapper;
import com.safetrip.backend.infrastructure.persistence.repository.PolicyPlanJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PolicyPlanRepositoryImpl implements PolicyPlanRepository {

    private final PolicyPlanJpaRepository policyPlanJpaRepository;

    @Override
    public PolicyPlan save(PolicyPlan policyPlan) {
        log.debug("💾 Guardando plan de póliza");
        PolicyPlanEntity entity = PolicyPlanMapper.toEntity(policyPlan);
        PolicyPlanEntity savedEntity = policyPlanJpaRepository.save(entity);
        return PolicyPlanMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<PolicyPlan> findById(Long id) {
        log.debug("🔍 Buscando plan de póliza con id: {}", id);
        return policyPlanJpaRepository.findById(id)
                .map(PolicyPlanMapper::toDomain);
    }

    @Override
    public List<PolicyPlan> findAll() {
        log.debug("📋 Obteniendo todos los planes de póliza");
        return policyPlanJpaRepository.findAll()
                .stream()
                .map(PolicyPlanMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        log.debug("🗑️ Eliminando plan de póliza con id: {}", id);
        policyPlanJpaRepository.deleteById(id);
    }

    @Override
    public List<PolicyPlan> findByPolicyTypeId(Long policyTypeId) {
        log.debug("🔍 Buscando planes para tipo de póliza: {}", policyTypeId);
        return policyPlanJpaRepository.findByPolicyTypeId(policyTypeId)
                .stream()
                .map(PolicyPlanMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PolicyPlan> findByPolicyTypeIdAndActive(Long policyTypeId, Boolean active) {
        log.debug("🔍 Buscando planes para tipo {} con estado activo: {}", policyTypeId, active);
        return policyPlanJpaRepository.findByPolicyTypeIdAndActive(policyTypeId, active)
                .stream()
                .map(PolicyPlanMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PolicyPlan> findByPax(Integer pax) {
        log.debug("🔍 Buscando planes con {} pax", pax);
        return policyPlanJpaRepository.findByPax(pax)
                .stream()
                .map(PolicyPlanMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PolicyPlan> findByPolicyTypeIdAndPax(Long policyTypeId, Integer pax) {
        log.debug("🔍 Buscando planes para tipo {} y {} pax", policyTypeId, pax);
        return policyPlanJpaRepository.findByPolicyTypeIdAndPax(policyTypeId, pax)
                .stream()
                .map(PolicyPlanMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PolicyPlan> findAllActive() {
        log.debug("✅ Obteniendo planes activos");
        return policyPlanJpaRepository.findAllActive()
                .stream()
                .map(PolicyPlanMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PolicyPlan> findAllPopular() {
        log.debug("⭐ Obteniendo planes populares");
        return policyPlanJpaRepository.findAllPopular()
                .stream()
                .map(PolicyPlanMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Page<PolicyPlan> findAllOrderByCreatedAtDesc(Pageable pageable) {
        log.debug("📊 Obteniendo planes paginados");
        Page<PolicyPlanEntity> entitiesPage = policyPlanJpaRepository
                .findAllByOrderByCreatedAtDesc(pageable);

        List<PolicyPlan> content = entitiesPage.getContent().stream()
                .map(PolicyPlanMapper::toDomain)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, entitiesPage.getTotalElements());
    }

    @Override
    public Page<PolicyPlan> findByActiveOrderByCreatedAtDesc(Boolean active, Pageable pageable) {
        log.debug("📊 Obteniendo planes {} paginados", active ? "activos" : "inactivos");
        Page<PolicyPlanEntity> entitiesPage = policyPlanJpaRepository
                .findByActiveOrderByCreatedAtDesc(active, pageable);

        List<PolicyPlan> content = entitiesPage.getContent().stream()
                .map(PolicyPlanMapper::toDomain)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, entitiesPage.getTotalElements());
    }
}