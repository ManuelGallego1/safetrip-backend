package com.safetrip.backend.application.service.impl;

import com.safetrip.backend.domain.model.*;
import com.safetrip.backend.domain.repository.*;
import com.safetrip.backend.infrastructure.integration.notification.resend.client.EmailClient;
import com.safetrip.backend.infrastructure.integration.notification.resend.dto.EmailAttachment;
import com.safetrip.backend.application.usecase.GeneratePolicyPdfUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para ejecutar tareas asíncronas post-pago
 * Usa @Async para mantener una sesión de Hibernate activa
 */
@Service
public class AsyncPaymentTaskService {

    private static final Logger log = LoggerFactory.getLogger(AsyncPaymentTaskService.class);

    private final PolicyRepository policyRepository;
    private final PaymentRepository paymentRepository;
    private final PolicyFileRepository policyFileRepository;
    private final FileRepository fileRepository;
    private final FileStorageRepository fileStorageRepository;
    private final EmailClient emailClient;
    private final GeneratePolicyPdfUseCase generatePolicyPdfUseCase;

    public AsyncPaymentTaskService(
            PolicyRepository policyRepository,
            PaymentRepository paymentRepository,
            PolicyFileRepository policyFileRepository,
            FileRepository fileRepository,
            FileStorageRepository fileStorageRepository,
            EmailClient emailClient,
            GeneratePolicyPdfUseCase generatePolicyPdfUseCase) {
        this.policyRepository = policyRepository;
        this.paymentRepository = paymentRepository;
        this.policyFileRepository = policyFileRepository;
        this.fileRepository = fileRepository;
        this.fileStorageRepository = fileStorageRepository;
        this.emailClient = emailClient;
        this.generatePolicyPdfUseCase = generatePolicyPdfUseCase;
    }

    /**
     * Ejecuta tareas post-pago de forma asíncrona en nueva transacción
     * @Async asegura que se ejecute en un thread separado
     * @Transactional(readOnly = true) mantiene la sesión de Hibernate activa
     */
    @Async
    @Transactional(readOnly = true)
    public void executePostPaymentTasks(Long policyId, Long paymentId) {
        try {
            log.info("🔄 Iniciando tareas post-pago para póliza {}", policyId);

            // Cargar la póliza con todas sus relaciones en esta nueva transacción
            Policy policy = policyRepository.findById(policyId).orElse(null);
            if (policy == null) {
                log.error("❌ No se pudo obtener la póliza {} para tareas post-pago", policyId);
                return;
            }

            // Buscar el pago en la base de datos
            Payment payment = paymentRepository.findById(paymentId).orElse(null);
            if (payment == null) {
                log.error("❌ No se pudo obtener el pago {} para tareas post-pago", paymentId);
                return;
            }

            // Tarea 1: Enviar email interno con documentos adjuntos
            sendInternalPolicyEmail(policy, payment, policyId);

            // Tarea 2: Generar PDF y enviar al cliente
            sendCustomerPolicyPdf(policy, payment, policyId);

            log.info("✅ Tareas post-pago completadas para póliza {}", policyId);

        } catch (Exception e) {
            log.error("❌ Error en tareas post-pago para póliza {}: {}",
                    policyId, e.getMessage(), e);
        }
    }

    /**
     * Envía email interno con documentos adjuntos
     */
    private void sendInternalPolicyEmail(Policy policy, Payment payment, Long policyId) {
        try {
            List<EmailAttachment> attachments = getAndValidatePolicyFiles(policyId);

            if (!attachments.isEmpty()) {
                emailClient.sendPolicyEmail(policy, payment, attachments);
                log.info("✅ Email interno enviado con {} adjunto(s)", attachments.size());
            } else {
                log.info("📭 No hay archivos para enviar en email interno");
            }
        } catch (Exception e) {
            log.error("⚠️ Error enviando email interno (póliza {}): {}",
                    policyId, e.getMessage());
        }
    }

    /**
     * Genera PDF y lo envía al cliente
     */
    private void sendCustomerPolicyPdf(Policy policy, Payment payment, Long policyId) {
        try {
            byte[] pdfBytes = generatePolicyPdfWithoutAuth(policyId, payment.getUser());

            if (pdfBytes != null && pdfBytes.length > 0) {
                emailClient.sendPolicyPdfToCustomer(policy, payment, pdfBytes);
                log.info("✅ PDF enviado al cliente: {}",
                        policy.getCreatedByUser().getEmail());
            } else {
                log.warn("⚠️ No se generó PDF para enviar al cliente");
            }
        } catch (Exception e) {
            log.error("⚠️ Error generando/enviando PDF al cliente (póliza {}): {}",
                    policyId, e.getMessage());
        }
    }

    /**
     * Genera PDF sin validar autenticación
     */
    private byte[] generatePolicyPdfWithoutAuth(Long policyId, User user) {
        try {
            return generatePolicyPdfUseCase.executeWithoutAuth(policyId, user);
        } catch (Exception e) {
            log.error("❌ Error generando PDF: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene archivos de una póliza desde MinIO
     */
    private List<EmailAttachment> getAndValidatePolicyFiles(Long policyId) {
        List<EmailAttachment> attachments = new ArrayList<>();

        try {
            List<PolicyFile> policyFiles = policyFileRepository.findByPolicyId(policyId);

            if (policyFiles == null || policyFiles.isEmpty()) {
                log.debug("No hay archivos adjuntos en la póliza: {}", policyId);
                return attachments;
            }

            for (PolicyFile policyFile : policyFiles) {
                try {
                    Long fileId = policyFile.getFileId();

                    File file = fileRepository.findById(fileId)
                            .orElseThrow(() -> new IllegalArgumentException("Archivo no encontrado: " + fileId));

                    if (!isAllowedContentType(file.getContentType())) {
                        log.info("Archivo omitido (no es imagen ni PDF): {} ({})",
                                file.getOriginalName(), file.getContentType());
                        continue;
                    }

                    byte[] fileContent = fileStorageRepository.download(file.getFileName());

                    attachments.add(new EmailAttachment(
                            file.getOriginalName(),
                            fileContent,
                            file.getContentType()
                    ));

                    log.info("Archivo agregado al correo: {} ({} bytes)",
                            file.getOriginalName(), fileContent.length);

                } catch (IllegalArgumentException e) {
                    log.warn("Archivo rechazado para póliza {}: {}", policyId, e.getMessage());
                } catch (Exception e) {
                    log.warn("Error procesando archivo en póliza {}: {}", policyId, e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Error obteniendo archivos de la póliza {}: {}", policyId, e.getMessage());
        }

        return attachments;
    }

    /**
     * Valida que el tipo MIME sea permitido para email
     */
    private boolean isAllowedContentType(String contentType) {
        if (contentType == null) return false;
        return contentType.startsWith("image/") || contentType.equals("application/pdf");
    }
}