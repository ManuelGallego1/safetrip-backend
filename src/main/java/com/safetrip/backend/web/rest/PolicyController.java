package com.safetrip.backend.web.rest;

import com.safetrip.backend.application.service.PaymentService;
import com.safetrip.backend.application.service.PolicyService;
import com.safetrip.backend.web.dto.request.ConfirmPaymentRequest;
import com.safetrip.backend.web.dto.request.CreatePolicyRequest;
import com.safetrip.backend.web.dto.response.ApiResponse;
import com.safetrip.backend.web.dto.response.CreatePolicyResponse;
import com.safetrip.backend.web.dto.response.PolicyResponse;
import com.safetrip.backend.web.dto.response.PolicyResponseWithDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
}