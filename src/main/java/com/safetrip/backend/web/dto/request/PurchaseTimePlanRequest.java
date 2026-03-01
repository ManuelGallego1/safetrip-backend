package com.safetrip.backend.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseTimePlanRequest {
    private Long policyPlanId;
    private Integer rooms;
    private Long paymentTypeId;
}