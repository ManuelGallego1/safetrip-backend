package com.safetrip.backend.infrastructure.integration.zurich.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
public class InfoPasajeroResponse {
    @JsonProperty("nombrePax")
    private String nombrePax;

    @JsonProperty("apellidoPax")
    private String apellidoPax;

    @JsonProperty("fechaNacimientoPax")
    private LocalDate fechaNacimientoPax;

    @JsonProperty("documentPax")
    private String documentPax;

    @JsonProperty("correoPax")
    private String correoPax;

    @JsonProperty("mensajeCondicionesMedicas")
    private String mensajeCondicionesMedicas;
}
