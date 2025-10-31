package com.safetrip.backend.application.service;

import com.safetrip.backend.domain.model.File;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileAppService {
    /**
     * Sube un archivo y lo asocia con un usuario
     * @param userId ID del usuario
     * @param file Archivo a subir
     * @return Modelo de dominio del archivo guardado
     * @throws IOException Si hay error leyendo el archivo
     */
    File uploadFileForUser(Long userId, MultipartFile file) throws IOException;

    /**
     * Sube un archivo y lo asocia con una póliza
     * @param policyId ID de la póliza
     * @param file Archivo a subir
     * @return Modelo de dominio del archivo guardado
     * @throws IOException Si hay error leyendo el archivo
     */
    File uploadFileForPolicy(Long policyId, MultipartFile file) throws IOException;

    /**
     * Descarga un archivo por su ID
     * @param fileId ID del archivo
     * @return Datos binarios del archivo
     */
    byte[] downloadFile(Long fileId);

    /**
     * Elimina un archivo y todas sus relaciones
     * @param fileId ID del archivo a eliminar
     */
    void deleteFile(Long fileId);
}