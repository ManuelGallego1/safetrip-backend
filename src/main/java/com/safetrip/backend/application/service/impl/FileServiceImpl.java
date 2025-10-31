package com.safetrip.backend.application.service.impl;

import com.safetrip.backend.application.service.FileService;
import com.safetrip.backend.domain.repository.FileStorageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * Implementación básica de FileService
 * @deprecated Usar FileAppService en su lugar
 */
@Slf4j
@Service
@Deprecated
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileStorageRepository storageRepository;

    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        log.info("📤 Subiendo archivo: {}", file.getOriginalFilename());
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        String uploadedFileName = storageRepository.upload(fileName, file.getContentType(), file.getBytes());
        log.info("✅ Archivo subido: {}", uploadedFileName);
        return uploadedFileName;
    }

    @Override
    public byte[] downloadFile(String fileName) {
        log.info("⬇️ Descargando archivo: {}", fileName);
        byte[] data = storageRepository.download(fileName);
        log.info("✅ Archivo descargado: {} bytes", data.length);
        return data;
    }

    @Override
    public void deleteFile(String fileName) {
        log.info("🗑️ Eliminando archivo: {}", fileName);
        storageRepository.delete(fileName);
        log.info("✅ Archivo eliminado: {}", fileName);
    }
}