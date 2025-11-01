package com.safetrip.backend.web.rest;

import com.safetrip.backend.application.service.PaymentService;
import com.safetrip.backend.application.service.PolicyService;
import com.safetrip.backend.web.dto.request.ConfirmPaymentRequest;
import com.safetrip.backend.web.dto.request.CreatePolicyRequest;
import com.safetrip.backend.web.dto.response.*;
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

@Slf4j
@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;
    private final PaymentService paymentService;

    /**
     * Crea una póliza preliminar (manual, Excel o imagen)
     * El tipo de fuente se detecta automáticamente según los datos recibidos
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CreatePolicyResponse>> createPreliminaryPolicy(
            @RequestPart("request") CreatePolicyRequest request,
            @RequestPart(value = "dataFile", required = false) MultipartFile dataFile,
            @RequestPart(value = "attachments", required = false) MultipartFile[] attachments
    ) throws IOException {

        CreatePolicyResponse response = policyService.createPreliminaryPolicy(
                request,
                dataFile,
                attachments
        );

        return ResponseEntity.ok(
                ApiResponse.success("Póliza preliminar creada exitosamente", response)
        );
    }

    /**
     * Callback de confirmación de pago desde la pasarela
     */
    @GetMapping("/payment-confirmation")
    public ResponseEntity<ApiResponse<PolicyResponse>> confirmPayment(
            @RequestParam(required = true) String voucher,
            @RequestParam(required = true, name = "status_card") String statusCard,
            @RequestParam(required = false, defaultValue = "N/A") String message) {

        log.info("🔔 Confirmación de pago recibida - Voucher: {}, Status: {}", voucher, statusCard);

        ConfirmPaymentRequest request = new ConfirmPaymentRequest(voucher, statusCard, message);
        PolicyResponse response = paymentService.confirmPayment(request);

        return ResponseEntity.ok(
                ApiResponse.success("Pago confirmado exitosamente", response)
        );
    }

    /**
     * Obtiene todas las pólizas del usuario autenticado
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PolicyResponseWithDetails>>> getAllPolicies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        ApiResponse<List<PolicyResponseWithDetails>> response =
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
    public ResponseEntity<ApiResponse<List<InsuredPersonResponse>>> getInsuredPersons(
            @PathVariable Long policyId) {

        log.info("📋 Solicitud de asegurados para la póliza: {}", policyId);

        try {
            ApiResponse<List<InsuredPersonResponse>> response =
                    policyService.getInsuredPersonsByPolicy(policyId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException ex) {
            log.warn("⚠️ {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ex.getMessage(), null));

        } catch (SecurityException ex) {
            log.error("🚫 Acceso denegado: {}", ex.getMessage());
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("Acceso denegado: " + ex.getMessage(), null));

        } catch (Exception ex) {
            log.error("❌ Error obteniendo asegurados: {}", ex.getMessage(), ex);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error interno del servidor", null));
        }
    }
}