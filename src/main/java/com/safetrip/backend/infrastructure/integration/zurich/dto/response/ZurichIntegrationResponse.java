package com.safetrip.backend.infrastructure.integration.zurich.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ZurichIntegrationResponse {
    private String username;
    private String password;
    private String baseUrl;
}