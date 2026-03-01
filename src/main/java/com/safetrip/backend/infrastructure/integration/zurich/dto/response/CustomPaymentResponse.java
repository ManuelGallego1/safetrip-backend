package com.safetrip.backend.infrastructure.integration.zurich.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomPaymentResponse {

    private String status;

    private String currency;

    private BigDecimal amount;

    @JsonProperty("qr_img")
    private String qrImg;

    private String link;

    @JsonProperty("link_id")
    private Long linkId;

    private String voucher;
}