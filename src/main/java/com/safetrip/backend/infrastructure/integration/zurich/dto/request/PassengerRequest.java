package com.safetrip.backend.infrastructure.integration.zurich.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PassengerInfoDTO {

    private String nombrePax;
    private String apellidoPax;
    private LocalDate fechaNacimientoPax;
    private String documentPax;
    private String correoPax;
    private String mensajeCondicionesMedicas;
}