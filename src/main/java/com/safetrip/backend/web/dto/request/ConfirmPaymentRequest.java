package com.safetrip.backend.web.dto.request;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConfirmPaymentRequest {
    private String voucher;
    private String status;
    private String message;
}