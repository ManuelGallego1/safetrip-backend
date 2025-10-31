package com.safetrip.backend.infrastructure.integration.zurich.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AddOrderPaymentRequest {
    private List<PassengerRequest> infoPasajeros;
    private double costo;
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
    private Integer tasaCambio;
    private List<Object> upgrades;
}