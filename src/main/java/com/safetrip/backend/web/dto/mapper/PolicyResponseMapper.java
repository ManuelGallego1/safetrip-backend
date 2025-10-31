package com.safetrip.backend.application.mapper;

import com.safetrip.backend.domain.model.Policy;
import com.safetrip.backend.web.dto.response.PolicyResponse;

public class PolicyResponseMapper {

    private PolicyResponseMapper() {
        // Evita instanciación
    }

    public static PolicyResponse toDto(Policy policy) {
        return new PolicyResponse(
                policy.getPolicyId(),
                policy.getPolicyNumber(),
                policy.getPolicyType() != null ? policy.getPolicyType().getName() : null,
                policy.getPersonCount(),
                policy.getCreatedByUser() != null ? policy.getCreatedByUser().getUserId() : null,
                policy.getCreatedByUser() != null ? policy.getCreatedByUser().getPerson().getFullName() : null,
                policy.getCreatedAt(),
                policy.getUpdatedAt(),
                policy.getCreatedWithFile()
        );
    }
}