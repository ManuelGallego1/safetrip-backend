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
public class WalletBalanceResponse {
    private Long walletId;
    private String walletTypeName;
    private BigDecimal balance;
    private BigDecimal consumed;
    private BigDecimal total;
    private Integer availablePax;
    private Integer consumedPax;
    private Integer totalPax;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private Boolean isActive;
    private String transactionId;
}