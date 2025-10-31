package com.safetrip.backend.web.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PolicyResponseWithDetails {
    private Long policyId;
    private String policyNumber;
    private String policyTypeName;
    private Integer personCount;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private Boolean createdWithFile;

    // Detalles del viaje
    private String origin;
    private String destination;
    private ZonedDateTime departure;
    private ZonedDateTime arrival;

    //detalles de pago
    private BigDecimal unitPrice;
}
