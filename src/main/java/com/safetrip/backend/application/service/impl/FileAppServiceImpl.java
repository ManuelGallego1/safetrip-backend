package com.safetrip.backend.application.service.impl;

import com.safetrip.backend.application.service.FileAppService;
import com.safetrip.backend.domain.model.File;
import com.safetrip.backend.domain.model.PolicyFile;
import com.safetrip.backend.domain.model.UserFile;
import com.safetrip.backend.domain.repository.FileRepository;
import com.safetrip.backend.domain.repository.FileStorageRepository;
import com.safetrip.backend.domain.repository.PolicyFileRepository;
import com.safetrip.backend.domain.repository.UserFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileAppServiceImpl implements FileAppService {

    private final FileRepository fileRepository;
    private final PolicyFileRepository policyFileRepository;
    private final UserFileRepository userFileRepository;
    private final FileStorageRepository fileStorageRepository;

    @Value("${minio.bucket:safetrip-policies}")
    private String defaultBucket;

    @Override
    @Transactional
    public File uploadFileForUser(Long userId, MultipartFile multipartFile) throws IOException {
        log.info("📤 Subiendo archivo para usuario {}: {}", userId, multipartFile.getOriginalFilename());

        // 1. Validar y procesar archivo
        File savedFile = processAndSaveFile(multipartFile);

        // 2. Crear relación con usuario
        UserFile userFile = new UserFile(null, userId, savedFile.getFileId());
        userFileRepository.save(userFile);
        log.info("✅ Relación usuario-archivo creada: userId={}, fileId={}", userId, savedFile.getFileId());

        return savedFile;
    }

    @Override
    @Transactional
    public File uploadFileForPolicy(Long policyId, MultipartFile multipartFile) throws IOException {
        log.info("📤 Subiendo archivo para póliza {}: {}", policyId, multipartFile.getOriginalFilename());

        // 1. Validar y procesar archivo
        File savedFile = processAndSaveFile(multipartFile);

        // 2. Crear relación con póliza
        PolicyFile policyFile = new PolicyFile(null, policyId, savedFile.getFileId());
        policyFileRepository.save(policyFile);
        log.info("✅ Relación póliza-archivo creada: policyId={}, fileId={}", policyId, savedFile.getFileId());

        return savedFile;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadFile(Long fileId) {
        log.info("⬇️ Descargando archivo: {}", fileId);

        // 1. Buscar metadatos del archivo
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("Archivo no encontrado: " + fileId));

        // 2. Descargar del storage
        byte[] data = fileStorageRepository.download(file.getFileName());
        log.info("✅ Archivo descargado: {} ({} bytes)", file.getFileName(), data.length);

        return data;
    }

    @Override
    @Transactional
    public void deleteFile(Long fileId) {
        log.info("🗑️ Eliminando archivo: {}", fileId);

        // 1. Buscar archivo
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("Archivo no encontrado: " + fileId));

        // 2. Eliminar del storage
        try {
            fileStorageRepository.delete(file.getFileName());
            log.debug("☁️ Archivo eliminado del storage: {}", file.getFileName());
        } catch (Exception e) {
            log.warn("⚠️ Error eliminando archivo del storage (continuando): {}", e.getMessage());
        }

        // 3. Eliminar de la base de datos (las relaciones se eliminarán en cascada si está configurado)
        fileRepository.delete(fileId);
        log.info("✅ Archivo eliminado de BD: {}", fileId);
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Procesa y guarda un archivo: valida, sube al storage y guarda metadatos
     */
    private File processAndSaveFile(MultipartFile multipartFile) throws IOException {
        // 1. Validar archivo
        validateFile(multipartFile);

        String originalFilename = multipartFile.getOriginalFilename();

        // 2. Generar nombre único
        String uniqueFileName = generateUniqueFileName(originalFilename);
        log.debug("📝 Nombre único generado: {}", uniqueFileName);

        // 3. Subir a MinIO/S3
        byte[] fileData = multipartFile.getBytes();
        fileStorageRepository.upload(uniqueFileName, multipartFile.getContentType(), fileData);
        log.debug("☁️ Archivo subido a storage: {}", uniqueFileName);

        // 4. Crear modelo de dominio
        File file = new File(
                null,
                uniqueFileName,
                originalFilename,
                multipartFile.getContentType(),
                defaultBucket,
                multipartFile.getSize(),
                ZonedDateTime.now()
        );

        // 5. Guardar metadatos en BD
        File savedFile = fileRepository.save(file);
        log.info("✅ Archivo guardado en BD: ID={}, nombre={}", savedFile.getFileId(), savedFile.getFileName());

        return savedFile;
    }

    /**
     * Valida que el archivo sea válido
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("El archivo no tiene nombre");
        }

        // Validar tamaño (ejemplo: máximo 10MB)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException(
                    String.format("El archivo excede el tamaño máximo permitido (10MB). Tamaño: %.2f MB",
                            file.getSize() / (1024.0 * 1024.0))
            );
        }

        // Validar tipo de archivo (opcional)
        String contentType = file.getContentType();
        if (contentType != null && !isAllowedContentType(contentType)) {
            throw new IllegalArgumentException(
                    "Tipo de archivo no permitido: " + contentType
            );
        }
    }

    /**
     * Verifica si el tipo de contenido está permitido
     */
    private boolean isAllowedContentType(String contentType) {
        // Lista de tipos MIME permitidos
        return contentType.startsWith("image/") ||
                contentType.startsWith("application/pdf") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                contentType.equals("application/vnd.ms-excel") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                contentType.equals("application/msword");
    }

    /**
     * Genera un nombre único para el archivo
     */
    private String generateUniqueFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return String.format("%s_%d%s",
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                extension
        );
    }

    /**
     * Extrae la extensión del archivo
     */
    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDotIndex = filename.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : filename.substring(lastDotIndex);
    }
}