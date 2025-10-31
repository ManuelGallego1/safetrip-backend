package com.safetrip.backend.application.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Servicio básico para operaciones de archivos sin asociaciones
 * @deprecated Usar FileAppService en su lugar para funcionalidad completa
 */
@Deprecated
public interface FileService {
    String uploadFile(MultipartFile file) throws IOException;
    byte[] downloadFile(String fileName);
    void deleteFile(String fileName);
}
