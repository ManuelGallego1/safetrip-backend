package com.safetrip.backend.web.rest;

import com.safetrip.backend.application.service.WalletMoneyRechargeService;
import com.safetrip.backend.web.dto.request.RechargeWalletRequest;
import com.safetrip.backend.web.dto.response.ApiResponse;
import com.safetrip.backend.web.dto.response.PurchasePlanResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/wallet-money")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class WalletMoneyController {

    private final WalletMoneyRechargeService walletMoneyRechargeService;

    /**
     * Recarga una wallet con un monto específico
     */
    @PostMapping("/recharge")
    public ResponseEntity<ApiResponse<PurchasePlanResponse>> rechargeWallet(
            @RequestBody RechargeWalletRequest request) {

        log.info("💵 Solicitud de recarga de wallet: ${}", request.getAmount());

        try {
            PurchasePlanResponse response = walletMoneyRechargeService.rechargeWallet(request);

            return ResponseEntity.ok(
                    ApiResponse.success("Link de pago generado exitosamente", response)
            );
        } catch (IllegalArgumentException e) {
            log.error("❌ Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), null));
        } catch (Exception e) {
            log.error("❌ Error recargando wallet: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error interno al procesar la recarga", null));
        }
    }

    /**
     * Callback de confirmación de pago para recargas de wallet
     */
    @GetMapping("/payment-confirmation")
    public ResponseEntity<ApiResponse<String>> confirmRechargePayment(
            @RequestParam(required = true) String voucher,
            @RequestParam(required = true, name = "status_card") String statusCard) {

        log.info("🔔 Confirmación de pago de recarga - Voucher: {}", voucher);

        try {
            walletMoneyRechargeService.confirmRechargePayment(voucher, statusCard);

            return ResponseEntity.ok(
                    ApiResponse.success("Recarga confirmada exitosamente", voucher)
            );
        } catch (Exception e) {
            log.error("❌ Error confirmando recarga: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }
}