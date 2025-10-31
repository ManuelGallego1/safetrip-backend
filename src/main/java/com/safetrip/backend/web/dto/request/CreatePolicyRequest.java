package com.safetrip.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CreatePolicyDTO {
    private Long policyTypeId;
    private Integer personCount;
    private Long createdByUserId;
    private List<PersonDTO> persons;
}
