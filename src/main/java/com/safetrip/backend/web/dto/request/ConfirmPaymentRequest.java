package com.safetrip.backend.application.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConfirmPaymentDTO {
    private Long paymentMethodId;
    private BigDecimal appliedAmount;
    private Long paymentReference;
}