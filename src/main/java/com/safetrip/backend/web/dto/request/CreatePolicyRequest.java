package com.safetrip.backend.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CreatePolicyRequest {
    private String policyType;
    private List<PersonRequest> persons;
}