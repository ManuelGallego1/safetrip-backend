package com.safetrip.backend.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileResponse {
    private Long fileId;
    private String fileName;
    private String originalName;
    private String contentType;
    private String bucket;
    private Long size;
    private String fileUrl;
    private ZonedDateTime createdAt;

    // Constructor simplificado para respuestas básicas
    public FileResponse(Long fileId, String fileName, String contentType, Long size) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.contentType = contentType;
        this.size = size;
    }
}
