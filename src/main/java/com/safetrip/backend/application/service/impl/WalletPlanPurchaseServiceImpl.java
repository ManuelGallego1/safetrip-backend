package com.safetrip.backend.application.service.impl;

import com.safetrip.backend.application.service.WalletPlanPurchaseService;
import com.safetrip.backend.domain.model.*;
import com.safetrip.backend.domain.model.enums.PaymentStatus;
import com.safetrip.backend.domain.repository.*;
import com.safetrip.backend.infrastructure.integration.zurich.client.PaymentOrderClient;
import com.safetrip.backend.infrastructure.integration.zurich.dto.request.CustomPaymentRequest;
import com.safetrip.backend.infrastructure.integration.zurich.dto.response.CustomPaymentResponse;
import com.safetrip.backend.infrastructure.integration.zurich.dto.response.CustomStatusPaymentResponse;
import com.safetrip.backend.infrastructure.integration.zurich.dto.response.StatusPaymentResponse;
import com.safetrip.backend.web.dto.request.PurchasePlanRequest;
import com.safetrip.backend.web.dto.response.PurchasePlanResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletPlanPurchaseServiceImpl implements WalletPlanPurchaseService {

    private final PolicyPlanRepository policyPlanRepository;
    private final WalletRepository walletRepository;
    private final WalletTypeRepository walletTypeRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentTypeRepository paymentTypeRepository;
    private final PaymentOrderClient paymentOrderClient;

    @Value("${zurich.confirmation-url.custom-ap}")
    private String confirmationUrl;

    private static final Long WALLET_TYPE_PAX = 1L;

    @Override
    @Transactional
    public PurchasePlanResponse purchasePlan(PurchasePlanRequest request) {
        log.info("🛒 Iniciando compra de plan: {}", request.getPolicyPlanId());

        User currentUser = getAuthenticatedUser();

        // 1. Obtener el plan y calcular el precio
        PolicyPlan plan = policyPlanRepository.findById(request.getPolicyPlanId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Plan no encontrado: " + request.getPolicyPlanId()));

        if (!plan.getActive()) {
            throw new IllegalArgumentException("El plan seleccionado no está activo");
        }

        BigDecimal totalAmount = plan.getPolicyType().getBaseValue()
                .multiply(BigDecimal.ONE.subtract(
                        plan.getDiscountPercentage().divide(BigDecimal.valueOf(100))))
                .multiply(BigDecimal.valueOf(plan.getPax()))
                .setScale(2, RoundingMode.HALF_UP);

        log.info("💰 Monto calculado: {} para {} pax con {}% descuento",
                totalAmount, plan.getPax(), plan.getDiscountPercentage());

        // 2. Generar link de pago con customPayment
        CustomPaymentRequest customPaymentRequest = new CustomPaymentRequest();

        String formattedAmount = totalAmount.setScale(0, RoundingMode.HALF_UP).toString();

        log.info("💵 Amount formateado para Zurich: {}", formattedAmount);

        customPaymentRequest.setAmount(formattedAmount);
        customPaymentRequest.setPlanName(String.format(
                "Plan %d pax - %s%% descuento",
                plan.getPax(),
                plan.getDiscountPercentage()
        ));
        customPaymentRequest.setCurrency("COP");
        customPaymentRequest.setHotelName(currentUser.getPerson().getFullName());
        customPaymentRequest.setNit(currentUser.getPerson().getDocumentType() + currentUser.getPerson().getDocumentNumber());
        customPaymentRequest.setConfirmationUrl(confirmationUrl);

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

        // 3. Crear pre-wallet vacía (sin transactionId, pero CON pax)
        WalletType walletType = walletTypeRepository.findById(WALLET_TYPE_PAX)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de wallet PAX no encontrado"));

        Wallet wallet = new Wallet(
                null,
                walletType,
                currentUser,
                BigDecimal.valueOf(plan.getPax()),
                ZonedDateTime.now(),
                ZonedDateTime.now(),
                null,
                null,
                null
        );

        Wallet savedWallet = walletRepository.save(wallet);
        log.info("💼 Pre-wallet creada: ID {} con {} pax", savedWallet.getWalletId(), plan.getPax());

        // 4. Crear Payment PENDING con voucher
        PaymentType paymentType = paymentTypeRepository.findById(request.getPaymentTypeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tipo de pago no encontrado: " + request.getPaymentTypeId()));

        Payment payment = new Payment(
                null,
                paymentType,
                PaymentStatus.PENDING,
                voucher,
                totalAmount,
                currentUser,
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );

        Payment savedPayment = paymentRepository.save(payment);
        log.info("💳 Payment creado: ID {} con voucher {}", savedPayment.getPaymentId(), voucher);

        // 5. Crear WalletTransaction
        WalletTransaction transaction = new WalletTransaction(
                null,
                savedPayment,
                savedWallet,
                true,
                BigDecimal.valueOf(plan.getPax()),
                BigDecimal.ZERO,
                String.format("Compra de plan %d pax con %s%% descuento",
                        plan.getPax(), plan.getDiscountPercentage()),
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );

        WalletTransaction savedTransaction = walletTransactionRepository.save(transaction);
        log.info("📝 WalletTransaction creada: ID {}", savedTransaction.getWalletTransactionId());

        log.info("✅ Compra de plan procesada exitosamente");

        return PurchasePlanResponse.builder()
                .paymentUrl(paymentLink)
                .walletId(savedWallet.getWalletId())
                .paymentId(savedPayment.getPaymentId())
                .totalAmount(totalAmount)
                .message("Link de pago generado exitosamente")
                .build();
    }

    @Override
    @Transactional
    public void confirmPlanPayment(String voucher, String statusCard) {
        log.info("🔔 Confirmando pago de plan - Voucher: {}", voucher);

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
                processSuccessfulPlanPayment(payment, walletTransaction, voucher);
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
                processFailedPlanPayment(payment, paymentStatus, statusResponse.getPaymentDescription());
                throw new RuntimeException(
                        String.format("El pago no fue aprobado. Estado: %s - %s",
                                paymentStatus, statusResponse.getPaymentDescription())
                );

            default:
                log.warn("⚠️ Estado de pago desconocido: {}", paymentStatus);
                throw new RuntimeException("Estado de pago no reconocido: " + paymentStatus);
        }
    }

    private void processSuccessfulPlanPayment(Payment payment, WalletTransaction walletTransaction, String voucher) {
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

        // 2. Actualizar Wallet con el voucher como transactionId
        Wallet wallet = walletTransaction.getWallet();

        int walletUpdated = walletRepository.updateTransactionId(
                wallet.getWalletId(),
                voucher,
                now
        );

        if (walletUpdated == 0) {
            throw new RuntimeException("No se pudo actualizar la wallet con el voucher");
        }

        log.info("💼 Wallet {} actualizada con transactionId: {}", wallet.getWalletId(), voucher);

        // 3. Actualizar WalletTransaction con balance
        walletTransactionRepository.updateTransactionOnConfirm(
                walletTransaction.getWalletTransactionId(),
                walletTransaction.getTotal(),
                "Pago confirmado - Voucher: " + voucher,
                now
        );

        log.info("✅ Pago confirmado exitosamente. Wallet {} actualizada con voucher {}",
                wallet.getWalletId(), voucher);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void processFailedPlanPayment(Payment payment, String paymentStatus, String paymentDescription) {
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