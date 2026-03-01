package com.safetrip.backend.infrastructure.integration.notification.resend.dto;

import java.util.Arrays;
import java.util.List;

/**
 * DTO para adjuntos en emails
 */
public class EmailAttachment {
    private String filename;
    private byte[] content;
    private String contentType;

    // Tipos de archivo permitidos
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "xlsx", "xls", "png", "jpg", "jpeg", "gif", "bmp");
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-excel",
            "image/png",
            "image/jpeg",
            "image/gif",
            "image/bmp"
    );

    public EmailAttachment(String filename, byte[] content, String contentType) {
        this.filename = filename;
        this.content = content;
        this.contentType = contentType;
        validateAttachment();
    }

    private void validateAttachment() {
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("El nombre del archivo no puede estar vacío");
        }

        String extension = getFileExtension(filename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    String.format("Tipo de archivo no permitido: %s. Solo se aceptan: %s",
                            extension, ALLOWED_EXTENSIONS)
            );
        }

        if (!ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    String.format("MIME type no permitido: %s", contentType)
            );
        }

        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("El archivo está vacío");
        }
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1) : "";
    }

    // Getters
    public String getFilename() {
        return filename;
    }

    public byte[] getContent() {
        return content;
    }

    public String getContentType() {
        return contentType;
    }
}
