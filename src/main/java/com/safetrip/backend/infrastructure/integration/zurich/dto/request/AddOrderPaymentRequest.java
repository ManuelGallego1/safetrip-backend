package com.safetrip.backend.infrastructure.integration.zurich.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class AddOrderPaymentRequest {
    private List<PassengerInfoDTO> infoPasajeros;
    private BigDecimal costo;
    private LocalDate fechaSalida;
    private LocalDate fechaLlegada;
    private String referencia;
    private String moneda;
    private Integer pasajeros;
    private String nombreContacto;
    private String telefonoContacto;
    private String emailContacto;
    private String consideracionesGenerales;
    private Integer emision;
    private Integer plan;
    private Integer paisDestino;
    private Integer paisOrigen;
    private BigDecimal tasaCambio;
    private List<Object> upgrades;
}