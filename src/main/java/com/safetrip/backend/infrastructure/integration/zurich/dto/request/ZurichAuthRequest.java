package com.safetrip.backend.infrastructure.integration.zurich.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZurichAuthRequest {
    private String username;
    private String password;
}
