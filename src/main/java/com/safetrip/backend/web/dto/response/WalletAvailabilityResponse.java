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
public class WalletAvailabilityResponse {
    private Boolean hasWallets;
    private Boolean canPayWithWallet;
    private BigDecimal totalBalance;
    private BigDecimal requiredAmount;
    private Integer availablePax;
    private Integer requiredPax;
    private String walletTypeName;
    private Long walletTypeId;
    private String message;
}