package com.safetrip.backend.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletRechargeRequest {
    private Long walletTypeId;
    private BigDecimal amount;
    private String hotelName;
    private String nit;
    private Long discountId;
}