package com.safetrip.backend.application.dto;

import com.safetrip.backend.web.dto.request.PersonPolicyRequest;

import java.time.ZonedDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentDTO {
    private Long policyTypeId;
    private Long paymentTypeId;
    private Long policyId;
    private ZonedDateTime departure;
    private ZonedDateTime arrival;
    private List<PersonPolicyRequest> persons;
}