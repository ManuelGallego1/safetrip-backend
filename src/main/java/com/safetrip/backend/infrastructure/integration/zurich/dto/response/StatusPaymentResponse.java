package com.safetrip.backend.infrastructure.integration.zurich.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class StatusPaymentResponse {

    private Long id;

    private Integer origen;

    private Integer destino;

    private LocalDate salida;

    private LocalDate retorno;

    @JsonProperty("nombre_contacto")
    private String nombreContacto;

    @JsonProperty("email_contacto")
    private String emailContacto;

    @JsonProperty("telefono_contacto")
    private String telefonoContacto;

    private Integer producto;

    @JsonProperty("nombre_agencia")
    private String nombreAgencia;

    private BigDecimal total;

    private String codigo;

    private LocalDate fecha;

    private String status;

    @JsonProperty("desc_status")
    private String descStatus;

    @JsonProperty("origin_ip")
    private String originIp;

    @JsonProperty("family_plan")
    private Boolean familyPlan;

    private String referencia;

    @JsonProperty("info_pasajeros")
    private List<InfoPasajeroResponse> infoPasajeros;
}