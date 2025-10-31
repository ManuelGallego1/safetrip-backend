package com.safetrip.backend.web.dto.mapper;

import com.safetrip.backend.domain.model.Policy;
import com.safetrip.backend.domain.model.PolicyDetail;
import com.safetrip.backend.domain.model.PolicyType;
import com.safetrip.backend.web.dto.response.PolicyResponse;
import com.safetrip.backend.web.dto.response.PolicyResponseWithDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Component
public class PolicyResponseMapper {

    private static final Logger log = LoggerFactory.getLogger(PolicyResponseMapper.class);

    /**
     * Convierte una entidad Policy a PolicyResponse (básico).
     */
    public PolicyResponse toDto(Policy policy) {
        if (policy == null) {
            return null;
        }

        log.trace("📝 Mapping policy ID: {}", policy.getPolicyId());

        // Obtener datos básicos
        Long policyId = policy.getPolicyId();
        String policyNumber = policy.getPolicyNumber();
        Integer personCount = policy.getPersonCount();
        ZonedDateTime createdAt = policy.getCreatedAt();
        ZonedDateTime updatedAt = policy.getUpdatedAt();
        Boolean createdWithFile = policy.getCreatedWithFile();
        BigDecimal unitPriceWhitDiscuit = policy.getUnitPriceWithDiscount();

        // Obtener policy type name de forma segura
        String policyTypeName = null;
        try {
            PolicyType policyType = policy.getPolicyType();
            if (policyType != null) {
                policyTypeName = policyType.getName();
            }
        } catch (Exception ex) {
            log.warn("⚠️ Could not load policy type for policy {}: {}", policyId, ex.getMessage());
        }

        return new PolicyResponse(
                policyId,
                policyNumber,
                policyTypeName,
                personCount,
                createdAt,
                updatedAt,
                createdWithFile,
                unitPriceWhitDiscuit
        );
    }

    /**
     * Convierte una entidad Policy a PolicyResponseWithDetails (incluye detalles).
     */
    public PolicyResponseWithDetails toDtoWithDetails(Policy policy, PolicyDetail policyDetail) {
        if (policy == null) {
            log.warn("⚠️ Attempted to map null policy with details");
            return null;
        }

        try {
            log.trace("📝 Mapping policy with details - ID: {}", policy.getPolicyId());

            PolicyResponse baseResponse = toDto(policy);

            if (baseResponse == null) {
                return null;
            }

            PolicyResponseWithDetails response = new PolicyResponseWithDetails(
                    baseResponse.getPolicyId(),
                    baseResponse.getPolicyNumber(),
                    baseResponse.getPolicyTypeName(),
                    baseResponse.getPersonCount(),
                    baseResponse.getCreatedAt(),
                    baseResponse.getUpdatedAt(),
                    baseResponse.getCreatedWithFile(),
                    policyDetail != null ? policyDetail.getOrigin() : null,
                    policyDetail != null ? policyDetail.getDestination() : null,
                    policyDetail != null ? policyDetail.getArrival() : null,
                    policyDetail != null ? policyDetail.getDeparture() : null,
                    baseResponse.getUnitPrice()
            );

            log.trace("  ✅ Successfully mapped policy with details - ID: {}", policy.getPolicyId());
            return response;

        } catch (Exception ex) {
            log.error("❌ Error mapping policy with details to DTO: {}", ex.getMessage(), ex);
            throw ex;
        }
    }
}