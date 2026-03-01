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
public class PurchasePlanResponse {
    private String paymentUrl;
    private Long walletId;
    private Long paymentId;
    private BigDecimal totalAmount;
    private String message;
}