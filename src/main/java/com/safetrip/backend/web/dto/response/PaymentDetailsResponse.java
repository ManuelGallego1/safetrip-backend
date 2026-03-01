package com.safetrip.backend.web.dto.response;

import com.safetrip.backend.domain.model.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetailsResponse {
    private Long id;
    private String transactionId;
    private BigDecimal amount;
    private PaymentStatus status;
    private ZonedDateTime date;
    private String paymentTypeName;

    private String UserEmail;
    private String UserName;
}