package com.safetrip.backend.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
public class PolicyResponse {
    private Long policyId;
    private String policyNumber;
    private String policyTypeName;
    private Integer personCount;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private Boolean createdWithFile;
    private BigDecimal unitPrice;
}