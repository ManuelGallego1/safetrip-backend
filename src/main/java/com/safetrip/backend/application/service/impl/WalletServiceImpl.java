package com.safetrip.backend.application.service.impl;

import com.safetrip.backend.application.service.WalletService;
import com.safetrip.backend.domain.model.User;
import com.safetrip.backend.domain.model.Wallet;
import com.safetrip.backend.domain.model.WalletTransaction;
import com.safetrip.backend.domain.repository.WalletRepository;
import com.safetrip.backend.domain.repository.WalletTransactionRepository;
import com.safetrip.backend.web.dto.response.TransactionDetailResponse;
import com.safetrip.backend.web.dto.response.WalletBalanceResponse;
import com.safetrip.backend.web.dto.response.WalletConsumptionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<WalletBalanceResponse> getUserWalletBalances() {
        log.info("🔍 Obteniendo balances agrupados por tipo de wallet");

        User currentUser = getAuthenticatedUser();

        // Filtrar solo wallets con transaction_id no nulo
        List<Wallet> wallets = walletRepository.findAllByUserId(currentUser.getUserId())
                .stream()
                .filter(w -> w.getTransactionId() != null && !w.getTransactionId().isEmpty())
                .collect(Collectors.toList());

        if (wallets.isEmpty()) {
            return List.of();
        }

        return wallets.stream()
                .collect(Collectors.groupingBy(w -> w.getWalletType().getWalletTypeId()))
                .values()
                .stream()
                .map(this::buildGroupedWalletBalance)
                .collect(Collectors.toList());
    }

    private WalletBalanceResponse buildGroupedWalletBalance(List<Wallet> wallets) {
        Wallet firstWallet = wallets.get(0);
        Long walletTypeId = firstWallet.getWalletType().getWalletTypeId();
        String walletTypeName = firstWallet.getWalletType().getName();

        // Obtener todas las transacciones de todas las wallets del mismo tipo
        List<WalletTransaction> transactions = wallets.stream()
                .flatMap(w -> walletTransactionRepository
                        .findByWalletId(w.getWalletId()).stream())
                .collect(Collectors.toList());

        // Calcular totales agregados
        BigDecimal totalInitial = transactions.stream()
                .filter(WalletTransaction::getIncome)
                .map(WalletTransaction::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal currentBalance = wallets.stream()
                .map(Wallet::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal consumed = totalInitial.subtract(currentBalance);

        WalletBalanceResponse.WalletBalanceResponseBuilder builder =
                WalletBalanceResponse.builder()
                        .walletTypeName(walletTypeName)
                        .balance(currentBalance)
                        .total(totalInitial)
                        .consumed(consumed);

        // Configuración específica por tipo
        switch (walletTypeId.intValue()) {
            case 1: // PAX
                builder.availablePax(currentBalance.intValue())
                        .consumedPax(consumed.intValue())
                        .totalPax(totalInitial.intValue())
                        .isActive(currentBalance.compareTo(BigDecimal.ZERO) > 0);
                break;

            case 2: // SUBSCRIPTION
                // Para suscripciones, buscar la wallet más reciente activa
                ZonedDateTime now = ZonedDateTime.now();

                Wallet activeSubscription = wallets.stream()
                        .filter(w -> w.getStartDate() != null && w.getEndDate() != null)
                        .filter(w -> !now.isBefore(w.getStartDate()) && !now.isAfter(w.getEndDate()))
                        .filter(w -> w.getTotal().compareTo(BigDecimal.ZERO) > 0)
                        .max((w1, w2) -> w1.getEndDate().compareTo(w2.getEndDate()))
                        .orElse(null);

                if (activeSubscription != null) {
                    // Hay suscripción activa
                    builder.startDate(activeSubscription.getStartDate())
                            .endDate(activeSubscription.getEndDate())
                            .isActive(true)
                            .balance(activeSubscription.getTotal())
                            .total(activeSubscription.getTotal())
                            .consumed(BigDecimal.ZERO);
                } else {
                    // No hay suscripción activa, buscar la más reciente (incluso expirada)
                    Wallet lastSubscription = wallets.stream()
                            .filter(w -> w.getStartDate() != null && w.getEndDate() != null)
                            .max((w1, w2) -> w1.getEndDate().compareTo(w2.getEndDate()))
                            .orElse(firstWallet);

                    builder.startDate(lastSubscription.getStartDate())
                            .endDate(lastSubscription.getEndDate())
                            .isActive(false)
                            .balance(BigDecimal.ZERO)
                            .total(BigDecimal.ZERO)
                            .consumed(BigDecimal.ZERO);
                }
                break;

            case 3: // CASH
                builder.isActive(currentBalance.compareTo(BigDecimal.ZERO) > 0);
                break;

            default:
                builder.isActive(currentBalance.compareTo(BigDecimal.ZERO) > 0);
        }

        return builder.build();
    }

    @Override
    @Transactional(readOnly = true)
    public WalletConsumptionResponse getWalletConsumption(Long walletId) {
        log.info("🔍 Obteniendo consumo de wallet: {}", walletId);

        User currentUser = getAuthenticatedUser();
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Wallet no encontrada: " + walletId
                ));

        // Verificar que la wallet pertenece al usuario
        if (!wallet.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new SecurityException("No tienes permiso para ver esta wallet");
        }

        // Verificar que tenga transaction_id
        if (wallet.getTransactionId() == null || wallet.getTransactionId().isEmpty()) {
            throw new IllegalArgumentException("Wallet sin transacción válida");
        }

        // Obtener todas las transacciones de la wallet
        List<WalletTransaction> transactions = walletTransactionRepository
                .findByWalletId(walletId);

        // Calcular balance inicial (suma de todos los ingresos)
        BigDecimal initialBalance = transactions.stream()
                .filter(WalletTransaction::getIncome)
                .map(WalletTransaction::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Balance actual
        BigDecimal currentBalance = wallet.getTotal();

        // Total consumido
        BigDecimal totalConsumed = initialBalance.subtract(currentBalance);

        // Porcentaje consumido
        int consumptionPercentage = initialBalance.compareTo(BigDecimal.ZERO) > 0
                ? totalConsumed.multiply(BigDecimal.valueOf(100))
                .divide(initialBalance, 0, RoundingMode.HALF_UP)
                .intValue()
                : 0;

        // Última transacción
        ZonedDateTime lastTransactionAt = transactions.stream()
                .map(WalletTransaction::getCreatedAt)
                .max(ZonedDateTime::compareTo)
                .orElse(wallet.getCreatedAt());

        // Mapear transacciones con información de póliza
        List<TransactionDetailResponse> transactionDetails = transactions.stream()
                .map(this::buildTransactionDetail)
                .collect(Collectors.toList());

        WalletConsumptionResponse response = WalletConsumptionResponse.builder()
                .walletId(wallet.getWalletId())
                .walletTypeName(wallet.getWalletType().getName())
                .initialBalance(initialBalance)
                .currentBalance(currentBalance)
                .totalConsumed(totalConsumed)
                .consumptionPercentage(consumptionPercentage)
                .createdAt(wallet.getCreatedAt())
                .lastTransactionAt(lastTransactionAt)
                .transactions(transactionDetails)
                .build();

        log.info("✅ Consumo calculado: Inicial={}, Actual={}, Consumido={}, Porcentaje={}%",
                initialBalance, currentBalance, totalConsumed, consumptionPercentage);

        return response;
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private TransactionDetailResponse buildTransactionDetail(WalletTransaction transaction) {
        // Extraer número de póliza de la descripción
        String policyNumber = extractPolicyNumber(transaction.getDescription());

        return TransactionDetailResponse.builder()
                .transactionId(transaction.getWalletTransactionId())
                .description(transaction.getDescription())
                .amount(transaction.getTotal())
                .balanceAfter(transaction.getBalance())
                .isIncome(transaction.getIncome())
                .transactionDate(transaction.getCreatedAt())
                .policyNumber(policyNumber)
                .build();
    }

    private String extractPolicyNumber(String description) {
        if (description == null) {
            return null;
        }

        // Buscar patrones comunes de póliza
        // Ejemplo: "Póliza 12345", "Póliza: 12345", "Póliza #12345"
        String[] patterns = {
                "Póliza\\s*:?\\s*#?([A-Z0-9-]+)",
                "Policy\\s*:?\\s*#?([A-Z0-9-]+)",
                "POL\\s*:?\\s*#?([A-Z0-9-]+)"
        };

        for (String pattern : patterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern,
                    java.util.regex.Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher m = p.matcher(description);
            if (m.find()) {
                return m.group(1);
            }
        }

        return null;
    }

    private User getAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}