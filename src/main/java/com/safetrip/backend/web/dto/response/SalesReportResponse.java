package com.safetrip.backend.web.dto.response;

import com.safetrip.backend.domain.model.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesReportResponse {
    private String estado;
    private String transactionId;
    private BigDecimal monto;
    private String emailUsuario;
    private String telefono;
    private ZonedDateTime fechaCreacion;
    private ZonedDateTime fechaActualizacion;
}