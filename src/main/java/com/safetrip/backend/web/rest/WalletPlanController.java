package com.safetrip.backend.web.rest;

import com.safetrip.backend.application.service.WalletPlanPurchaseService;
import com.safetrip.backend.web.dto.request.PurchasePlanRequest;
import com.safetrip.backend.web.dto.response.ApiResponse;
import com.safetrip.backend.web.dto.response.PurchasePlanResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/wallet-plans")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class WalletPlanController {

    private final WalletPlanPurchaseService walletPlanPurchaseService;

    /**
     * Compra un plan de pólizas (genera wallet y link de pago)
     */
    @PostMapping("/purchase")
    public ResponseEntity<ApiResponse<PurchasePlanResponse>> purchasePlan(
            @RequestBody PurchasePlanRequest request) {

        log.info("🛒 Solicitud de compra de plan: {}", request.getPolicyPlanId());

        try {
            PurchasePlanResponse response = walletPlanPurchaseService.purchasePlan(request);

            return ResponseEntity.ok(
                    ApiResponse.success("Plan comprado exitosamente", response)
            );
        } catch (IllegalArgumentException e) {
            log.error("❌ Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), null));
        } catch (Exception e) {
            log.error("❌ Error comprando plan: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error interno al procesar la compra", null));
        }
    }

    /**
     * Callback de confirmación de pago para planes
     */
    @GetMapping("/payment-confirmation")
    public ResponseEntity<ApiResponse<String>> confirmPlanPayment(
            @RequestParam(required = true) String voucher,
            @RequestParam(required = true, name = "status_card") String statusCard) {

        log.info("🔔 Confirmación de pago de plan - Voucher: {}", voucher);

        try {
            walletPlanPurchaseService.confirmPlanPayment(voucher, statusCard);

            return ResponseEntity.ok(
                    ApiResponse.success("Pago confirmado exitosamente", voucher)
            );
        } catch (Exception e) {
            log.error("❌ Error confirmando pago: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }
}