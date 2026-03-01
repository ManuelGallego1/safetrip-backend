package com.safetrip.backend.application.service.impl;

import com.safetrip.backend.application.service.PolicyPlanService;
import com.safetrip.backend.domain.model.PolicyPlan;
import com.safetrip.backend.domain.repository.PolicyPlanRepository;
import com.safetrip.backend.web.dto.response.PolicyPlanResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyPlanServiceImpl implements PolicyPlanService {

    private final PolicyPlanRepository policyPlanRepository;

    // Constantes para tipos de póliza
    private static final Long POLICY_TYPE_PAX = 1L;
    private static final Long POLICY_TYPE_TIME = 2L;

    @Override
    public List<PolicyPlanResponse> getAllActivePlans() {
        log.debug("🔍 Obteniendo todos los planes activos");
        List<PolicyPlan> plans = policyPlanRepository.findAllActive();
        return plans.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PolicyPlanResponse> getPlansByPolicyType(Long policyTypeId) {
        log.debug("🔍 Obteniendo planes para tipo: {}", policyTypeId);
        List<PolicyPlan> plans = policyPlanRepository
                .findByPolicyTypeIdAndActive(policyTypeId, true);
        return plans.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PolicyPlanResponse> getPopularPlans() {
        log.debug("🔍 Obteniendo planes populares");
        List<PolicyPlan> plans = policyPlanRepository.findAllPopular();
        return plans.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PolicyPlanResponse> getActivePaxPlans() {
        log.debug("👥 Obteniendo planes de PAX activos (policyTypeId = 1)");
        List<PolicyPlan> plans = policyPlanRepository
                .findByPolicyTypeIdAndActive(POLICY_TYPE_PAX, true);

        log.debug("✅ {} planes de PAX encontrados", plans.size());

        return plans.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PolicyPlanResponse> getActiveTimePlans() {
        log.debug("⏰ Obteniendo planes de TIEMPO activos (policyTypeId = 2)");
        List<PolicyPlan> plans = policyPlanRepository
                .findByPolicyTypeIdAndActive(POLICY_TYPE_TIME, true);

        log.debug("✅ {} planes de TIEMPO encontrados", plans.size());

        return plans.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PolicyPlanResponse mapToResponse(PolicyPlan plan) {
        BigDecimal baseValue = plan.getPolicyType().getBaseValue();
        BigDecimal discount = plan.getDiscountPercentage();

        // Calcular precio final: baseValue * (1 - discount/100)
        BigDecimal discountMultiplier = BigDecimal.ONE
                .subtract(discount.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        BigDecimal finalPrice = baseValue
                .multiply(discountMultiplier)
                .setScale(0, RoundingMode.HALF_UP);

        return PolicyPlanResponse.builder()
                .policyPlanId(plan.getPolicyPlanId())
                .policyTypeId(plan.getPolicyType().getPolicyTypeId())
                .policyTypeName(plan.getPolicyType().getName())
                .pax(plan.getPax())
                .discountPercentage(plan.getDiscountPercentage())
                .description(plan.getDescription())
                .popular(plan.getPopular())
                .baseValue(baseValue)
                .finalPrice(finalPrice)
                .build();
    }
}