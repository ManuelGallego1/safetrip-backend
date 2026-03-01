package com.safetrip.backend.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletConsumptionResponse {
    private Long walletId;
    private String walletTypeName;
    private BigDecimal initialBalance; // Balance inicial
    private BigDecimal currentBalance; // Balance actual
    private BigDecimal totalConsumed; // Total consumido
    private Integer consumptionPercentage; // Porcentaje consumido
    private ZonedDateTime createdAt;
    private ZonedDateTime lastTransactionAt;
    private List<TransactionDetailResponse> transactions;
}