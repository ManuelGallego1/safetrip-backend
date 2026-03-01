package com.safetrip.backend.application.service.impl;

import com.safetrip.backend.application.service.WalletMoneyRechargeService;
import com.safetrip.backend.domain.model.*;
import com.safetrip.backend.domain.model.enums.PaymentStatus;
import com.safetrip.backend.domain.repository.*;
import com.safetrip.backend.infrastructure.integration.zurich.client.PaymentOrderClient;
import com.safetrip.backend.infrastructure.integration.zurich.dto.request.CustomPaymentRequest;
import com.safetrip.backend.infrastructure.integration.zurich.dto.response.CustomPaymentResponse;
import com.safetrip.backend.infrastructure.integration.zurich.dto.response.CustomStatusPaymentResponse;
import com.safetrip.backend.web.dto.request.RechargeWalletRequest;
import com.safetrip.backend.web.dto.response.PurchasePlanResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletMoneyRechargeServiceImpl implements WalletMoneyRechargeService {

    private final WalletRepository walletRepository;
    private final WalletTypeRepository walletTypeRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentTypeRepository paymentTypeRepository;
    private final PaymentOrderClient paymentOrderClient;

    @Value("${zurich.confirmation-url.custom-rc}")
    private String confirmationUrl;

    private static final Long WALLET_TYPE_MONEY = 3L; // Tipo de wallet RECARGABLE (dinero)

    @Override
    @Transactional
    public PurchasePlanResponse rechargeWallet(RechargeWalletRequest request) {
        log.info("💵 Iniciando recarga de wallet: ${}", request.getAmount());

        User currentUser = getAuthenticatedUser();

        // Validar monto mínimo
        if (request.getAmount().compareTo(BigDecimal.valueOf(1500)) < 0) {
            throw new IllegalArgumentException("El monto mínimo de recarga es $10,000 COP");
        }

        // Validar monto máximo
        if (request.getAmount().compareTo(BigDecimal.valueOf(8000000)) > 0) {
            throw new IllegalArgumentException("El monto máximo de recarga es $8,000,000 COP");
        }

        log.info("💰 Monto a recargar: ${} para usuario {}",
                request.getAmount(), currentUser.getUserId());

        // 1. Generar link de pago con customPayment
        CustomPaymentRequest customPaymentRequest = new CustomPaymentRequest();

        // Formatear el monto sin decimales
        String formattedAmount = request.getAmount().toBigInteger().toString();

        log.info("💵 Amount formateado para Zurich: {}", formattedAmount);

        customPaymentRequest.setAmount(formattedAmount);
        customPaymentRequest.setPlanName("Recarga de Wallet");
        customPaymentRequest.setCurrency("COP");
        customPaymentRequest.setConfirmationUrl(confirmationUrl);
        customPaymentRequest.setHotelName(currentUser.getPerson().getFullName());
        customPaymentRequest.setNit(currentUser.getPerson().getDocumentType() +
                currentUser.getPerson().getDocumentNumber());

        LocalDate today = LocalDate.now();
        customPaymentRequest.setStartDate(today);
        customPaymentRequest.setEndDate(today.plusYears(1));

        CustomPaymentResponse customPaymentResponse = paymentOrderClient.customPayment(customPaymentRequest);

        if (customPaymentResponse == null) {
            throw new RuntimeException("No se pudo generar el link de pago");
        }

        String voucher = customPaymentResponse.getVoucher();
        String paymentLink = customPaymentResponse.getLink();

        if (voucher == null || voucher.isEmpty()) {
            throw new RuntimeException("No se pudo generar el voucher de pago");
        }

        if (paymentLink == null || paymentLink.isEmpty()) {
            throw new RuntimeException("No se pudo generar el link de pago");
        }

        log.info("🎫 Voucher generado: {}", voucher);
        log.info("🔗 Link de pago generado: {}", paymentLink);

        // 2. CREAR PRE-WALLET sin transactionId y SIN monto (balance = 0)
        WalletType walletType = walletTypeRepository.findById(WALLET_TYPE_MONEY)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tipo de wallet RECARGABLE no encontrado (ID: 3)"));

        Wallet preWallet = new Wallet(
                null,
                walletType,
                currentUser,
                BigDecimal.ZERO, // ← Balance inicial en CERO
                ZonedDateTime.now(),
                ZonedDateTime.now(),
                null, // No tiene fecha inicio
                null, // No tiene fecha fin
                null  // transactionId se actualizará al confirmar
        );

        Wallet savedPreWallet = walletRepository.save(preWallet);
        log.info("💼 PRE-WALLET RECARGABLE creada: ID {} con balance inicial $0",
                savedPreWallet.getWalletId());

        // 3. Crear Payment PENDING con voucher
        PaymentType paymentType = paymentTypeRepository.findById(request.getPaymentTypeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tipo de pago no encontrado: " + request.getPaymentTypeId()));

        Payment payment = new Payment(
                null,
                paymentType,
                PaymentStatus.PENDING,
                voucher,
                request.getAmount(),
                currentUser,
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );

        Payment savedPayment = paymentRepository.save(payment);
        log.info("💳 Payment creado: ID {} con voucher {} por ${}",
                savedPayment.getPaymentId(), voucher, request.getAmount());

        // 4. Crear WalletTransaction con el monto a recargar
        WalletTransaction transaction = new WalletTransaction(
                null,
                savedPayment,
                savedPreWallet,
                true, // Es una carga (entrada de dinero)
                request.getAmount(), // El monto que se va a recargar
                BigDecimal.ZERO, // Balance inicial en cero (se actualizará al confirmar)
                String.format("Recarga de wallet por $%s", request.getAmount()),
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );

        WalletTransaction savedTransaction = walletTransactionRepository.save(transaction);
        log.info("📝 WalletTransaction creada: ID {} por ${}",
                savedTransaction.getWalletTransactionId(), request.getAmount());

        log.info("✅ Recarga de wallet procesada exitosamente (PRE-WALLET creada)");

        return PurchasePlanResponse.builder()
                .paymentUrl(paymentLink)
                .walletId(savedPreWallet.getWalletId())
                .paymentId(savedPayment.getPaymentId())
                .totalAmount(request.getAmount())
                .message("Link de pago generado exitosamente")
                .build();
    }

    @Override
    @Transactional
    public void confirmRechargePayment(String voucher, String statusCard) {
        log.info("🔔 Confirmando recarga de wallet - Voucher: {}", voucher);

        // 1. Buscar el payment por voucher
        Payment payment = paymentRepository.findByTransactionId(voucher)
                .orElseThrow(() -> new IllegalArgumentException("El pago no existe"));

        WalletTransaction walletTransaction = walletTransactionRepository.findByPayment(payment)
                .orElseThrow(() -> new IllegalArgumentException("La transacción no existe"));

        // 2. Verificar idempotencia
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            log.info("ℹ️ Pago ya confirmado anteriormente");
            return;
        }

        // 3. Verificar estado del pago en Zurich
        CustomStatusPaymentResponse statusResponse = paymentOrderClient.getStatusCustomPayment(voucher);

        log.info("📋 Estado del pago en Zurich - Status: {}, Descripción: {}",
                statusResponse.getPaymentStatus(), statusResponse.getPaymentDescription());

        String paymentStatus = statusResponse.getPaymentStatus();

        // 4. Manejar diferentes estados del pago
        switch (paymentStatus) {
            case "Aceptada":
                log.info("✅ Pago verificado exitosamente en Zurich - Estado: Aceptada");
                processSuccessfulRecharge(payment, walletTransaction, voucher);
                break;

            case "Pendiente":
                log.info("⏳ Pago aún pendiente - No se actualizará el estado");
                return;

            case "Fallida":
            case "Abandonada":
            case "Cancelada":
            case "Rechazada":
                log.error("❌ Pago no exitoso - Status: {}, Descripción: {}",
                        paymentStatus, statusResponse.getPaymentDescription());
                processFailedRecharge(payment, paymentStatus, statusResponse.getPaymentDescription());
                throw new RuntimeException(
                        String.format("El pago no fue aprobado. Estado: %s - %s",
                                paymentStatus, statusResponse.getPaymentDescription())
                );

            default:
                log.warn("⚠️ Estado de pago desconocido: {}", paymentStatus);
                throw new RuntimeException("Estado de pago no reconocido: " + paymentStatus);
        }
    }

    private void processSuccessfulRecharge(Payment payment, WalletTransaction walletTransaction, String voucher) {
        ZonedDateTime now = ZonedDateTime.now();

        // 1. Actualizar Payment con estado COMPLETED
        int paymentUpdated = paymentRepository.updateStatus(
                payment.getPaymentId(),
                PaymentStatus.COMPLETED,
                now
        );

        if (paymentUpdated == 0) {
            throw new RuntimeException("No se pudo actualizar el estado del pago");
        }

        Wallet wallet = walletTransaction.getWallet();
        BigDecimal rechargeAmount = walletTransaction.getTotal();

        // 2. Obtener balance actual de la wallet
        BigDecimal currentBalance = wallet.getTotal() != null ? wallet.getTotal() : BigDecimal.ZERO;
        BigDecimal newBalance = currentBalance.add(rechargeAmount);

        log.info("💰 Balance actual: ${}, Recarga: ${}, Nuevo balance: ${}",
                currentBalance, rechargeAmount, newBalance);

        // 3. Actualizar Wallet con el voucher como transactionId Y el nuevo balance
        int walletUpdated = walletRepository.updateTransactionId(
                wallet.getWalletId(),
                voucher,
                now
        );

        if (walletUpdated == 0) {
            throw new RuntimeException("No se pudo actualizar la wallet con el voucher");
        }

        walletUpdated = walletRepository.updateBalance(
                wallet.getWalletId(),
                newBalance,
                now
        );

        if (walletUpdated == 0) {
            throw new RuntimeException("No se pudo actualizar la wallet con el balance");
        }

        log.info("💼 Wallet {} actualizada: transactionId={}, balance=${}",
                wallet.getWalletId(), voucher, newBalance);

        // 4. Actualizar WalletTransaction con el nuevo balance
        walletTransactionRepository.updateTransactionOnConfirm(
                walletTransaction.getWalletTransactionId(),
                newBalance, // El balance después de la recarga
                String.format("Recarga confirmada - Voucher: %s - Nuevo balance: $%s",
                        voucher, newBalance),
                now
        );

        log.info("✅ Recarga de wallet confirmada exitosamente. " +
                        "Wallet: {}, Monto recargado: ${}, Balance final: ${}",
                wallet.getWalletId(), rechargeAmount, newBalance);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processFailedRecharge(Payment payment, String paymentStatus, String paymentDescription) {
        ZonedDateTime now = ZonedDateTime.now();

        // Actualizar estado del pago a FAILED en transacción independiente
        int paymentUpdated = paymentRepository.updateStatus(
                payment.getPaymentId(),
                PaymentStatus.FAILED,
                now
        );

        if (paymentUpdated == 0) {
            log.error("⚠️ No se pudo actualizar el estado del pago a FAILED");
        } else {
            log.info("❌ Pago {} marcado como FAILED - Status: {}, Descripción: {}",
                    payment.getPaymentId(), paymentStatus, paymentDescription);
        }
    }

    private User getAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}