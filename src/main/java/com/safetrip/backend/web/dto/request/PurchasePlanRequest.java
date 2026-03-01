package com.safetrip.backend.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchasePlanRequest {
    private Long policyPlanId;
    private Long paymentTypeId;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
}