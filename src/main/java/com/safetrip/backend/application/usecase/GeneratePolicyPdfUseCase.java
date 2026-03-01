package com.safetrip.backend.application.usecase;

import com.safetrip.backend.application.service.FileAppService;
import com.safetrip.backend.domain.model.*;
import com.safetrip.backend.domain.repository.*;
import com.safetrip.backend.domain.service.PdfGeneratorService;
import com.safetrip.backend.infrastructure.integration.pdf.dto.Template01ApData;
import com.safetrip.backend.infrastructure.integration.pdf.dto.Template02ApData;
import com.safetrip.backend.infrastructure.integration.pdf.mapper.Template01ApMapper;
import com.safetrip.backend.infrastructure.integration.pdf.mapper.Template02ApMapper;
import com.safetrip.backend.infrastructure.integration.notification.resend.dto.EmailAttachment;
import com.safetrip.backend.infrastructure.integration.pdf.service.PolicyAttachmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneratePolicyPdfUseCase {

    private final PolicyRepository policyRepository;
    private final PolicyDetailRepository policyDetailRepository;
    private final PolicyPaymentRepository policyPaymentRepository;
    private final PolicyPersonRepository policyPersonRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final Template01ApMapper template01ApMapper;
    private final Template02ApMapper template02ApMapper;
    private final PolicyAttachmentService policyAttachmentService;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public byte[] execute(Long policyId) {
        log.info("🔍 Generando PDF completo para póliza ID: {} (con auth)", policyId);

        User currentUser = getAuthenticatedUser();
        Policy policy = getAuthorizedPolicy(policyId, currentUser);

        return generatePdfInternal(policy, currentUser);
    }


    /**
     * Método público para usuarios NO autenticados.
     * Puedes pasar solo policyId o agregar userId opcional.
     */
    @Transactional(readOnly = true)
    public byte[] executeWithoutAuth(Long policyId, User user) {
        log.info("🔍 Generando PDF completo para póliza ID: {} (sin auth)", policyId);

        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Póliza no encontrada con ID: " + policyId));

        return generatePdfInternal(policy, user);  // user = null
    }


    /**
     * Método privado reutilizable.
     * NO valida auth. NO valida permisos.
     * Solo genera el PDF como en el execute original.
     */
    private byte[] generatePdfInternal(Policy policy, User userOrNull) {

        Long policyId = policy.getPolicyId();
        PolicyDetail policyDetail = getPolicyDetail(policy);
        PolicyPayment policyPayment = getPolicyPayment(policy);

        // Adjuntos
        List<EmailAttachment> attachments = getPolicyAttachments(policyId);
        boolean isCreatedWithFiles = attachments != null && !attachments.isEmpty();

        // Personas
        List<PolicyPerson> policyPersons;
        if (isCreatedWithFiles) {
            log.info("📎 Póliza con archivos adjuntos: NO se cargarán personas aseguradas");
            policyPersons = Collections.emptyList();
        } else {
            policyPersons = getPolicyPersons(policyId);
            log.info("👥 Personas aseguradas encontradas: {}", policyPersons.size());
        }

        // Template 01
        Template01ApData template01Data = template01ApMapper.toTemplate01ApData(
                policy,
                policyDetail,
                policyPayment,
                policyPersons,
                userOrNull    // si es null, simplemente no se usa
        );

        // Template 02
        Template02ApData template02Data = template02ApMapper.toTemplate02ApData(
                policy,
                policyDetail,
                policyPayment,
                policyPersons,
                attachments
        );

        // Generar PDF final
        byte[] pdfBytes = pdfGeneratorService.generateCompletePolicyPdf(template01Data, template02Data);

        log.info("✅ PDF generado ({} bytes, modo: {})",
                pdfBytes.length,
                isCreatedWithFiles ? "archivos adjuntos" : "lista asegurados");

        return pdfBytes;
    }


    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User user)) {
            log.error("❌ Usuario autenticado no válido: {}", principal);
            throw new SecurityException("Usuario autenticado no válido");
        }
        return user;
    }

    private Policy getAuthorizedPolicy(Long policyId, User currentUser) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Póliza no encontrada"));
        if (!policy.getCreatedByUser().getUserId().equals(currentUser.getUserId())) {
            throw new SecurityException("No tiene permisos para descargar esta póliza");
        }
        return policy;
    }

    private PolicyDetail getPolicyDetail(Policy policy) {
        return policyDetailRepository.findByPolicyId(policy)
                .orElseThrow(() -> new IllegalStateException("No se encontró el detalle de la póliza"));
    }

    private PolicyPayment getPolicyPayment(Policy policy) {
        return policyPaymentRepository.findByPolicy(policy)
                .orElseThrow(() -> new IllegalStateException("No se encontró el pago de la póliza"));
    }

    private List<PolicyPerson> getPolicyPersons(Long policyId) {
        List<PolicyPerson> persons = policyPersonRepository.findByPolicyId(policyId);
        if (persons.isEmpty()) {
            log.warn("⚠️ No hay personas aseguradas para la póliza {}", policyId);
        }
        return persons;
    }

    private List<EmailAttachment> getPolicyAttachments(Long policyId) {
        return policyAttachmentService.getAttachments(policyId);
    }
}