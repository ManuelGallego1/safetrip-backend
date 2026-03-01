package com.safetrip.backend.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckWalletAvailabilityRequest {
    private Long policyTypeId;
    private Integer personCount;
    private Integer days;
}