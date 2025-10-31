package com.safetrip.backend.infrastructure.integration.zurich.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ZurichIntegrationConfigDTO {
    private String username;
    private String password;
    private String baseUrl;
}