package com.safetrip.backend.web.rest;

import com.safetrip.backend.application.service.WalletService;
import com.safetrip.backend.web.dto.response.ApiResponse;
import com.safetrip.backend.web.dto.response.WalletBalanceResponse;
import com.safetrip.backend.web.dto.response.WalletConsumptionResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}", allowCredentials = "true")
@SecurityRequirement(name = "bearerAuth")
public class WalletController {

    private final WalletService walletService;

    /**
     * GET /api/wallets/balances
     * Obtiene los balances de todas las wallets del usuario autenticado
     */
    @GetMapping("/balances")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<WalletBalanceResponse>>> getUserWalletBalances() {
        log.info("📊 GET /api/wallets/balances - Iniciando...");

        try {
            List<WalletBalanceResponse> balances = walletService.getUserWalletBalances();

            log.info("✅ GET /api/wallets/balances - {} wallets encontradas", balances.size());

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Balances obtenidos exitosamente",
                            balances
                    )
            );
        } catch (Exception e) {
            log.error("❌ Error en GET /api/wallets/balances", e);
            throw e;
        }
    }

    /**
     * GET /api/wallets/{walletId}/consumption
     * Obtiene el detalle de consumo de una wallet específica
     */
    @GetMapping("/{walletId}/consumption")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<WalletConsumptionResponse>> getWalletConsumption(
            @PathVariable Long walletId) {
        log.info("📊 GET /api/wallets/{}/consumption - Iniciando...", walletId);

        try {
            WalletConsumptionResponse consumption = walletService.getWalletConsumption(walletId);

            log.info("✅ GET /api/wallets/{}/consumption - Consumo calculado", walletId);

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Consumo obtenido exitosamente",
                            consumption
                    )
            );
        } catch (Exception e) {
            log.error("❌ Error en GET /api/wallets/{}/consumption", walletId, e);
            throw e;
        }
    }
}