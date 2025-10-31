package com.safetrip.backend.infrastructure.integration.zurich.dto.response;

import lombok.Data;

@Data
public class AddOrderPaymentResponse {
    private String message;
    private String codigo;
    private String ruta;
    private String valor;
}