package com.safetrip.backend.infrastructure.integration.zurich.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PassengerRequest {

    @JsonProperty("nombrePax")
    private String nombrePax;

    @JsonProperty("apellidoPax")
    private String apellidoPax;

    @JsonProperty("fechaNacimientoPax")
    private String fechaNacimientoPax;

    @JsonProperty("documentPax")
    private String documentPax;

    @JsonProperty("correoPax")
    private String correoPax;

    @JsonProperty("mensajeCondicionesMedicas")
    private String mensajeCondicionesMedicas;
}
