package com.safetrip.backend.application.mapper;

import com.safetrip.backend.domain.model.File;
import com.safetrip.backend.web.dto.response.FileResponse;

public class FileResponseMapper {

    private FileResponseMapper() {
    }

    public static FileResponse toDto(File file) {
        return new FileResponse(
                file.getFileId(),
                file.getFileName(),
                file.getContentType(),
                file.getFileSize()
        );
    }
}