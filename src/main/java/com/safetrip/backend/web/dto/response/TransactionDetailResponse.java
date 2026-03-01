package com.safetrip.backend.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetailResponse {
    private Long transactionId;
    private String description;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private Boolean isIncome; // true = ingreso, false = egreso
    private ZonedDateTime transactionDate;
    private String policyNumber; // Si está asociado a una póliza
}