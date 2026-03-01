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
public class WalletTransactionResponse {

    private Long transactionId;
    private String transactionType;
    private String walletTypeName;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String description;
    private String policyNumber;
    private ZonedDateTime createdAt;

    private Integer paxCount;
}