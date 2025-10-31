package com.safetrip.backend.web.dto.mapper;

import com.safetrip.backend.domain.model.File;
import com.safetrip.backend.web.dto.response.FileResponse;

public class FileResponseMapper {

    private FileResponseMapper() {
        // Constructor privado para clase utilitaria
    }

    /**
     * Convierte un modelo de dominio File a DTO de respuesta
     */
    public static FileResponse toDto(File file) {
        if (file == null) {
            return null;
        }

        return new FileResponse(
                file.getFileId(),
                file.getFileName(),
                file.getOriginalName(),
                file.getContentType(),
                file.getBucket(),
                file.getSize(),
                file.getFileUrl(),
                file.getCreatedAt()
        );
    }

    /**
     * Convierte un modelo de dominio File a DTO simplificado (sin metadatos completos)
     */
    public static FileResponse toSimpleDto(File file) {
        if (file == null) {
            return null;
        }

        return new FileResponse(
                file.getFileId(),
                file.getOriginalName(), // Usar nombre original para el usuario
                null, // No exponer nombre interno
                file.getContentType(),
                null, // No exponer bucket
                file.getSize(),
                file.getFileUrl(),
                file.getCreatedAt()
        );
    }
}