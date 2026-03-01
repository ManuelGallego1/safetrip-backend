package com.safetrip.backend.application.service;

import com.safetrip.backend.web.dto.response.PolicyPlanResponse;

import java.util.List;

public interface PolicyPlanService {

    List<PolicyPlanResponse> getAllActivePlans();
    List<PolicyPlanResponse> getPlansByPolicyType(Long policyTypeId);
    List<PolicyPlanResponse> getPopularPlans();
    List<PolicyPlanResponse> getActivePaxPlans();
    List<PolicyPlanResponse> getActiveTimePlans();
}