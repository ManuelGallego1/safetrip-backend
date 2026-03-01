package com.safetrip.backend.infrastructure.integration.pdf.service;

import com.safetrip.backend.domain.model.File;
import com.safetrip.backend.domain.model.PolicyFile;
import com.safetrip.backend.domain.repository.FileRepository;
import com.safetrip.backend.domain.repository.FileStorageRepository;
import com.safetrip.backend.domain.repository.PolicyFileRepository;
import com.safetrip.backend.infrastructure.integration.notification.resend.dto.EmailAttachment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyAttachmentService {

    private final PolicyFileRepository policyFileRepository;
    private final FileRepository fileRepository;
    private final FileStorageRepository fileStorageRepository;

    /**
     * Obtiene y valida archivos adjuntos asociados a una póliza.
     */
    public List<EmailAttachment> getAttachments(Long policyId) {
        List<EmailAttachment> attachments = new ArrayList<>();

        try {
            List<PolicyFile> policyFiles = policyFileRepository.findByPolicyId(policyId);
            if (policyFiles == null || policyFiles.isEmpty()) {
                log.debug("No se encontraron archivos asociados a la póliza {}", policyId);
                return attachments;
            }

            for (PolicyFile policyFile : policyFiles) {
                Long fileId = policyFile.getFileId();

                fileRepository.findById(fileId).ifPresentOrElse(file -> {
                    if (isAllowedContentType(file.getContentType())) {
                        try {
                            byte[] content = fileStorageRepository.download(file.getFileName());
                            attachments.add(new EmailAttachment(
                                    file.getOriginalName(),
                                    content,
                                    file.getContentType()
                            ));
                            log.debug("Archivo adjuntado: {} ({} bytes)", file.getOriginalName(), content.length);
                        } catch (Exception e) {
                            log.warn("Error descargando archivo {}: {}", fileId, e.getMessage());
                        }
                    } else {
                        log.info("Archivo omitido por tipo no permitido: {} ({})",
                                file.getOriginalName(), file.getContentType());
                    }
                }, () -> log.warn("Archivo no encontrado: {}", fileId));
            }

        } catch (Exception e) {
            log.error("Error al obtener archivos de la póliza {}: {}", policyId, e.getMessage());
        }

        return attachments;
    }

    private boolean isAllowedContentType(String contentType) {
        if (contentType == null) return false;
        return contentType.startsWith("image/") ||
                contentType.equals("application/pdf");
    }
}
