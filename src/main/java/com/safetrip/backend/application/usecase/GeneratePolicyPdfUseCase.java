package com.safetrip.backend.application.usecase;

import com.safetrip.backend.domain.model.*;
import com.safetrip.backend.domain.repository.*;
import com.safetrip.backend.domain.service.PdfGeneratorService;
import com.safetrip.backend.infrastructure.integration.pdf.dto.Template01ApData;
import com.safetrip.backend.infrastructure.integration.pdf.dto.Template02ApData;
import com.safetrip.backend.infrastructure.integration.pdf.mapper.Template01ApMapper;
import com.safetrip.backend.infrastructure.integration.pdf.mapper.Template02ApMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Genera el PDF COMPLETO de una póliza verificando permisos del usuario
     * (Template01 + Template02 con todos los asegurados)
     *
     * @param policyId ID de la póliza
     * @return Bytes del PDF completo generado
     * @throws IllegalArgumentException si la póliza no existe
     * @throws SecurityException        si el usuario no tiene permisos
     */
    @Transactional(readOnly = true)
    public byte[] execute(Long policyId) {
        log.info("🔍 Iniciando generación de PDF COMPLETO para póliza: {}", policyId);

        // 1. Obtener usuario autenticado
        User currentUser = getAuthenticatedUser();
        log.debug("Usuario autenticado: {} (ID: {})",
                currentUser.getEmail(), currentUser.getUserId());

        // 2. Buscar la póliza
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> {
                    log.error("❌ Póliza no encontrada: {}", policyId);
                    return new IllegalArgumentException(
                            "Póliza no encontrada con ID: " + policyId
                    );
                });

        // 3. Verificar que el usuario sea el creador de la póliza
        if (!policy.getCreatedByUser().getUserId().equals(currentUser.getUserId())) {
            log.error("❌ Usuario {} no autorizado para descargar póliza {}",
                    currentUser.getUserId(), policyId);
            throw new SecurityException(
                    "No tiene permisos para descargar esta póliza"
            );
        }

        log.debug("✅ Usuario autorizado para acceder a la póliza");

        // 4. Obtener el detalle de la póliza
        PolicyDetail policyDetail = policyDetailRepository
                .findByPolicyId(policy)
                .orElseThrow(() -> {
                    log.error("❌ Detalle de póliza no encontrado para póliza: {}", policyId);
                    return new IllegalStateException(
                            "No se encontró el detalle de la póliza"
                    );
                });

        // 5. Obtener el pago de la póliza
        PolicyPayment policyPayment = policyPaymentRepository
                .findByPolicy(policy)
                .orElseThrow(() -> {
                    log.error("❌ Pago no encontrado para póliza: {}", policyId);
                    return new IllegalStateException(
                            "No se encontró el pago de la póliza"
                    );
                });

        // 6. Obtener las personas aseguradas
        List<PolicyPerson> policyPersons = policyPersonRepository.findByPolicyId(policyId);

        if (policyPersons.isEmpty()) {
            log.error("❌ No se encontraron personas aseguradas para póliza: {}", policyId);
            throw new IllegalStateException(
                    "No se encontraron personas aseguradas en la póliza"
            );
        }

        log.debug("✅ Datos de póliza obtenidos correctamente: {} personas aseguradas",
                policyPersons.size());

        // 7. Mapear a Template01ApData
        Template01ApData template01Data = template01ApMapper.toTemplate01ApData(
                policy,
                policyDetail,
                policyPayment,
                policyPersons,
                currentUser
        );

        log.debug("✅ Datos mapeados a Template01ApData");

        // 8. Mapear a Template02ApData
        Template02ApData template02Data = template02ApMapper.toTemplate02ApData(
                policy,
                policyPayment,
                policyPersons
        );

        // 9. Generar PDF COMPLETO (Template01 + todas las páginas de Template02)
        byte[] pdfBytes = pdfGeneratorService.generateCompletePolicyPdf(
                template01Data,
                template02Data
        );

        log.info("🎉 PDF COMPLETO generado exitosamente para póliza {} - Tamaño: {} bytes - {} asegurados",
                policyId, pdfBytes.length, policyPersons.size());

        return pdfBytes;
    }

    /**
     * Obtiene el usuario autenticado del contexto de seguridad
     */
    private User getAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }
}