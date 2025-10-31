package com.safetrip.backend.infrastructure.integration.pdf.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PolicyPdfData {
    private String policyNumber;
    private String nameHotel;
    private String nameHotel2;
    private String nit;
    private String phoneNumber;
    private String address;
    private Integer personCount;

    // Fecha y hora de inicio
    private Integer hour;
    private Integer day;
    private Integer month;
    private Integer year;

    // Fecha y hora desde
    private Integer hourFrom;
    private Integer dayFrom;
    private Integer monthFrom;
    private Integer yearFrom;

    // Fecha y hora hasta
    private Integer dayTo;
    private Integer monthTo;
    private Integer yearTo;

    private String valuePolicy;
    private String collectivePolicy;
}