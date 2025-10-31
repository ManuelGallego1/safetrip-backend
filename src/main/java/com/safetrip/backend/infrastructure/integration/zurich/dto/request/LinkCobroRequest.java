package com.safetrip.backend.infrastructure.integration.zurich.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LinkCobroRequest {
    private String voucher;
    private String confirmationUrl;
    private String responseUrl;
}