package com.safetrip.backend.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreatePolicyResponse {
    private String url;
    private PolicyResponse policyResponse;
}