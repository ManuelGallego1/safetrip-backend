package com.safetrip.backend.infrastructure.integration.pdf.dto;

import com.safetrip.backend.infrastructure.integration.notification.resend.dto.EmailAttachment;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Template02ApData {
    private Long tipo;
    private String numPoliza;
    private String valor;
    private List<String> nombres;
    private List<String> documentos;
    private List<String> fechasDesde;
    private List<String> fechasHasta;
    private String plan;

    // Nuevos campos para soporte de archivos adjuntos
    private Boolean createdWithFiles;  // Indica si se usará template especial
    private List<EmailAttachment> attachedFiles; // Lista de archivos a adjuntar al PDF

    @Data
    @Builder
    public static class PageData {
        private Long tipo;
        private String numPoliza;
        private String valor;
        private List<String> nombres;
        private List<String> documentos;
        private List<String> fechasDesde;
        private List<String> fechasHasta;
        private int pageNumber;
        private int totalPages;
        private String plan;

        private List<Integer> lineasPorNombre;

        // Para el template especial con archivos
        private Boolean createdWithFiles;
    }
}