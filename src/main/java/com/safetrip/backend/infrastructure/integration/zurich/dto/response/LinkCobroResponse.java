package com.safetrip.backend.infrastructure.integration.zurich.dto.response;

import lombok.Data;

@Data
public class LinkCobroResponse {
    private String status;
    private String currency;
    private Double amount;
    private String qrImg;
    private String link;
    private Long linkId;
}