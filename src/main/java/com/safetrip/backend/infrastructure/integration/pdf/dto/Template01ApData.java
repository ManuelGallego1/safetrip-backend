package com.safetrip.backend.infrastructure.integration.pdf.dto;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class Template01ApData {

    // Número de póliza
    private String numPoliza;

    // Datos del tomador
    private String nomTomador;
    private String dir;
    private String celular;

    // Documento del tomador
    private String tipoDoc;
    private String numDoc;

    // Número de asegurados
    private String numAsegurados;

    // 🌎 FECHAS COMPLETAS (ZonedDateTime) - PRIORIDAD
    // Estas serán convertidas a Colombia en ITextPdfGenerator
    private ZonedDateTime fechaExpedicion;
    private ZonedDateTime fechaDesde;
    private ZonedDateTime fechaHasta;

    // 🔙 FALLBACK: Componentes de fecha individuales (String)
    // Solo se usan si los ZonedDateTime son null
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

    // Valor de la póliza
    private String valor;

    // Teléfono de emergencias
    private String telEmer;

    // Tipo de póliza (1 o 2)
    private Long tipo;

    // Indica si la vigencia empieza a la hora de expedición
    private Boolean hVigencia;
}