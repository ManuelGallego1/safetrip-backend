package com.safetrip.backend.web.rest;

import com.safetrip.backend.application.service.PaymentService;
import com.safetrip.backend.application.service.PolicyService;
import com.safetrip.backend.domain.model.PolicyType;
import com.safetrip.backend.domain.repository.PolicyTypeRepository;
import com.safetrip.backend.web.dto.request.ConfirmPaymentRequest;
import com.safetrip.backend.web.dto.request.CreatePolicyRequest;
import com.safetrip.backend.web.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PolicyController {

    private final PolicyService policyService;
    private final PaymentService paymentService;
    private final PolicyTypeRepository policyTypeRepository;

    @GetMapping("/config")
    public ResponseEntity<com.safetrip.backend.web.dto.response.ApiResponse<List<PolicyConfigResponse>>> getPolicyConfig() {
        log.info("📋 Obteniendo configuración de tipos de pólizas");

        try {
            List<PolicyType> policyTypes = policyTypeRepository.findAll();

            List<PolicyConfigResponse> configs = policyTypes.stream()
                    .map(pt -> PolicyConfigResponse.builder()
                            .policyTypeId(pt.getPolicyTypeId())
                            .name(pt.getName())
                            .baseValue(pt.getBaseValue())
                            .build())
                    .collect(Collectors.toList());

            log.info("✅ {} tipos de póliza encontrados", configs.size());

            return ResponseEntity.ok(
                    com.safetrip.backend.web.dto.response.ApiResponse.success("Configuración obtenida exitosamente", configs)
            );
        } catch (Exception e) {
            log.error("❌ Error obteniendo configuración: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(com.safetrip.backend.web.dto.response.ApiResponse.error("Error al obtener configuración", null));
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<com.safetrip.backend.web.dto.response.ApiResponse<CreatePolicyResponse>> createPreliminaryPolicy(
            @Parameter(
                    description = "Datos de la póliza en formato JSON",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreatePolicyRequest.class))
            )
            @RequestPart("request") CreatePolicyRequest request,

            @Parameter(
                    description = "Archivo de datos (Excel o imagen) con información de asegurados. Opcional si se envían datos manuales.",
                    required = false
            )
            @RequestPart(value = "dataFile", required = false) MultipartFile dataFile,

            @Parameter(
                    description = "Archivos adjuntos adicionales (documentos, comprobantes, etc.)",
                    required = false
            )
            @RequestPart(value = "attachments", required = false) MultipartFile[] attachments
    ) throws IOException {

        CreatePolicyResponse response = policyService.createPreliminaryPolicy(
                request,
                dataFile,
                attachments
        );

        return ResponseEntity.ok(
                com.safetrip.backend.web.dto.response.ApiResponse.success("Póliza preliminar creada exitosamente", response)
        );
    }

    @GetMapping("/payment-confirmation")
    public ResponseEntity<com.safetrip.backend.web.dto.response.ApiResponse<PolicyResponse>> confirmPayment(
            @Parameter(
                    description = "Número de voucher o identificador único de la transacción",
                    required = true,
                    example = "VCH-2024-001234"
            )
            @RequestParam(required = true) String voucher,

            @Parameter(
                    description = "Estado de la tarjeta/pago retornado por la pasarela",
                    required = true,
                    example = "approved"
            )
            @RequestParam(required = true, name = "status_card") String statusCard,

            @Parameter(
                    description = "Mensaje adicional o descripción del estado del pago",
                    required = false,
                    example = "Pago aprobado exitosamente"
            )
            @RequestParam(required = false, defaultValue = "N/A") String message
    ) {
        log.info("🔔 Confirmación de pago recibida - Voucher: {}, Status: {}", voucher, statusCard);

        ConfirmPaymentRequest request = new ConfirmPaymentRequest(voucher, statusCard, message);
        PolicyResponse response = paymentService.confirmPayment(request);

        return ResponseEntity.ok(
                com.safetrip.backend.web.dto.response.ApiResponse.success("Pago confirmado exitosamente", response)
        );
    }

    @GetMapping
    public ResponseEntity<com.safetrip.backend.web.dto.response.ApiResponse<List<PolicyResponseWithDetails>>> getAllPolicies(
            @Parameter(
                    description = "Número de página (basado en 0)",
                    example = "0"
            )
            @RequestParam(defaultValue = "0") int page,

            @Parameter(
                    description = "Cantidad de elementos por página",
                    example = "10"
            )
            @RequestParam(defaultValue = "10") int size
    ) {
        com.safetrip.backend.web.dto.response.ApiResponse<List<PolicyResponseWithDetails>> response =
                policyService.getAllPoliciesForUser(page, size);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> downloadPoliciesExcel() {
        log.info("📥 Solicitud de descarga de consolidado de pólizas en Excel");

        try {
            byte[] excelBytes = policyService.downloadPoliciesConsolidatedExcel();

            // Generar nombre de archivo con fecha actual
            String filename = String.format("consolidado_polizas_%s.xlsx", LocalDate.now());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(
                    ContentDisposition.attachment()
                            .filename(filename)
                            .build()
            );
            headers.setContentLength(excelBytes.length);

            log.info("✅ Excel descargado exitosamente: {} ({} bytes)", filename, excelBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes);

        } catch (IllegalArgumentException ex) {
            log.warn("⚠️ No hay pólizas para exportar: {}", ex.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception ex) {
            log.error("❌ Error descargando Excel: {}", ex.getMessage(), ex);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{policyId}/insured-persons")
    public ResponseEntity<com.safetrip.backend.web.dto.response.ApiResponse<List<InsuredPersonResponse>>> getInsuredPersons(
            @Parameter(
                    description = "ID único de la póliza",
                    required = true,
                    example = "123"
            )
            @PathVariable Long policyId
    ) {
        log.info("📋 Solicitud de asegurados para la póliza: {}", policyId);

        try {
            com.safetrip.backend.web.dto.response.ApiResponse<List<InsuredPersonResponse>> response =
                    policyService.getInsuredPersonsByPolicy(policyId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException ex) {
            log.warn("⚠️ {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(com.safetrip.backend.web.dto.response.ApiResponse.error(ex.getMessage(), null));

        } catch (SecurityException ex) {
            log.error("🚫 Acceso denegado: {}", ex.getMessage());
            return ResponseEntity.status(403)
                    .body(com.safetrip.backend.web.dto.response.ApiResponse.error("Acceso denegado: " + ex.getMessage(), null));

        } catch (Exception ex) {
            log.error("❌ Error obteniendo asegurados: {}", ex.getMessage(), ex);
            return ResponseEntity.internalServerError()
                    .body(com.safetrip.backend.web.dto.response.ApiResponse.error("Error interno del servidor", null));
        }
    }
}