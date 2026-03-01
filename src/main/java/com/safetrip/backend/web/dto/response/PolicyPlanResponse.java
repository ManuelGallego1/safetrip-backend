package com.safetrip.backend.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyPlanResponse {

    private Long policyPlanId;
    private Long policyTypeId;
    private String policyTypeName;
    private Integer pax;
    private BigDecimal discountPercentage;
    private String description;
    private Boolean popular;
    private BigDecimal baseValue;
    private BigDecimal finalPrice;
}