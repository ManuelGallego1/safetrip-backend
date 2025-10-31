package com.safetrip.backend.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class CreatePolicyRequest {
    private Long policyTypeId;
    private Integer personCount;
    private Long paymentTypeId;
    private String origin;
    private String destination;
    private ZonedDateTime departure;
    private ZonedDateTime arrival;
    private List<PersonPolicyRequest> persons;
}