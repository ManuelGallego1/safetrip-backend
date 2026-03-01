package com.safetrip.backend.web.rest;

import com.safetrip.backend.application.service.PaymentService;
import com.safetrip.backend.domain.model.Payment;
import com.safetrip.backend.domain.model.Wallet;
import com.safetrip.backend.web.dto.response.ApiResponse;
import com.safetrip.backend.web.dto.response.PaymentDetailsResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/wallets")
    public ResponseEntity<ApiResponse<List<Wallet>>> getAllWallets() {
        return null;
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<PaymentDetailsResponse>>> getUserPayments() {
        log.info("📋 Obteniendo pagos del usuario autenticado");

        try {
            ApiResponse<List<PaymentDetailsResponse>> response = paymentService.getUserPayments();
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("❌ Error obteniendo pagos: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), null));

        } catch (Exception e) {
            log.error("❌ Error inesperado obteniendo pagos: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error interno al obtener los pagos", null));
        }
    }
}
