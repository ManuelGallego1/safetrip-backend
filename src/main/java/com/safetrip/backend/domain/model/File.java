package com.safetrip.backend.domain.model;

import java.time.ZonedDateTime;

public class File {
    private final Long fileId;
    private final String fileName;
    private final String originalName;
    private final String contentType;
    private final String bucket;
    private final Long size;
    private final ZonedDateTime createdAt;

    public File(Long fileId, String fileName, String originalName,
                String contentType, String bucket, Long size, ZonedDateTime createdAt) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.originalName = originalName;
        this.contentType = contentType;
        this.bucket = bucket;
        this.size = size;
        this.createdAt = createdAt;
    }

    // Getters
    public Long getFileId() { return fileId; }
    public String getFileName() { return fileName; }
    public String getOriginalName() { return originalName; }
    public String getContentType() { return contentType; }
    public String getBucket() { return bucket; }
    public Long getSize() { return size; }
    public ZonedDateTime getCreatedAt() { return createdAt; }

    /**
     * Construye la URL completa del archivo
     * Ajusta según tu configuración (MinIO, S3, filesystem local)
     */
    public String getFileUrl() {
        // Para MinIO local
        return String.format("http://localhost:9000/%s/%s", bucket, fileName);

        // Para S3 de AWS
        // return String.format("https://s3.amazonaws.com/%s/%s", bucket, fileName);

        // Para filesystem local con servidor de archivos
        // return String.format("https://files.safetrip.com/%s/%s", bucket, fileName);
    }
}