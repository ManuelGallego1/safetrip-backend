package com.safetrip.backend.infrastructure.integration.pdf.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Template02ApData {

    // Número de póliza
    private String numPoliza;

    // Valor de la póliza
    private String valor;

    // Listas de asegurados
    private List<String> nombres;      // Lista de nombres completos
    private List<String> documentos;   // Lista de documentos (tipo + número)

    /**
     * Clase interna para representar una página de asegurados
     * Cada página puede tener hasta 20 asegurados
     */
    @Data
    @Builder
    public static class PageData {
        private String numPoliza;
        private String valor;
        private List<String> nombres;      // Máximo 20
        private List<String> documentos;   // Máximo 20
        private int pageNumber;            // Número de página (1, 2, 3...)
        private int totalPages;            // Total de páginas
    }
}