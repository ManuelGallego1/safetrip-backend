package com.safetrip.backend.web.rest;

import com.safetrip.backend.application.service.WalletTimePlanPurchaseService;
import com.safetrip.backend.web.dto.request.PurchaseTimePlanRequest;
import com.safetrip.backend.web.dto.response.ApiResponse;
import com.safetrip.backend.web.dto.response.PurchasePlanResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/wallet-time-plans")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class WalletTimePlanController {

    private final WalletTimePlanPurchaseService walletTimePlanPurchaseService;

    /**
     * Compra un plan por tiempo (mensual, semestral, anual)
     */
    @PostMapping("/purchase")
    public ResponseEntity<ApiResponse<PurchasePlanResponse>> purchaseTimePlan(
            @RequestBody PurchaseTimePlanRequest request) {

        log.info("🕒 Solicitud de compra de plan por tiempo: Plan ID {}, Habitaciones: {}",
                request.getPolicyPlanId(), request.getRooms());

        try {
            PurchasePlanResponse response = walletTimePlanPurchaseService.purchaseTimePlan(request);

            return ResponseEntity.ok(
                    ApiResponse.success("Plan por tiempo comprado exitosamente", response)
            );
        } catch (IllegalArgumentException e) {
            log.error("❌ Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), null));
        } catch (Exception e) {
            log.error("❌ Error comprando plan por tiempo: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error interno al procesar la compra", null));
        }
    }

    /**
     * Callback de confirmación de pago para planes por tiempo
     */
    @GetMapping("/payment-confirmation")
    public ResponseEntity<ApiResponse<String>> confirmTimePlanPayment(
            @RequestParam(required = true) String voucher,
            @RequestParam(required = true, name = "status_card") String statusCard) {

        log.info("🔔 Confirmación de pago de plan por tiempo - Voucher: {}", voucher);

        try {
            walletTimePlanPurchaseService.confirmTimePlanPayment(voucher, statusCard);

            return ResponseEntity.ok(
                    ApiResponse.success("Pago de plan por tiempo confirmado exitosamente", voucher)
            );
        } catch (Exception e) {
            log.error("❌ Error confirmando pago: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }
}