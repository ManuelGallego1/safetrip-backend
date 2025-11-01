package com.safetrip.backend.infrastructure.integration.pdf.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Template01ApData {
    private String numPoliza;
    private String nomTomador;
    private String dir;
    private String celular;
    private String tipoDoc;  // Bold
    private String numDoc;
    private String numAsegurados;
    private String hExp;
    private String diaExp;
    private String mesExp;
    private String anExp;
    private String hDesde;
    private String diaDesde;
    private String mesDesde;
    private String anDesde;
    private String diaHasta;
    private String mesHasta;
    private String anHasta;
    private String valor;
    private String telEmer;
}
