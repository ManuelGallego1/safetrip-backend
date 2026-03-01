package com.safetrip.backend.domain.repository;

import com.safetrip.backend.domain.model.PolicyPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PolicyPlanRepository {

    PolicyPlan save(PolicyPlan policyPlan);

    Optional<PolicyPlan> findById(Long id);

    List<PolicyPlan> findAll();

    void deleteById(Long id);

    List<PolicyPlan> findByPolicyTypeId(Long policyTypeId);

    List<PolicyPlan> findByPolicyTypeIdAndActive(Long policyTypeId, Boolean active);

    List<PolicyPlan> findByPax(Integer pax);

    List<PolicyPlan> findByPolicyTypeIdAndPax(Long policyTypeId, Integer pax);

    List<PolicyPlan> findAllActive();

    List<PolicyPlan> findAllPopular();

    Page<PolicyPlan> findAllOrderByCreatedAtDesc(Pageable pageable);

    Page<PolicyPlan> findByActiveOrderByCreatedAtDesc(Boolean active, Pageable pageable);
}