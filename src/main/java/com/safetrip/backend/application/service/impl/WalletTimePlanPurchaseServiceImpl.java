package com.safetrip.backend.application.service.impl;

import com.safetrip.backend.application.service.WalletTimePlanPurchaseService;
import com.safetrip.backend.domain.model.*;
import com.safetrip.backend.domain.model.enums.PaymentStatus;
import com.safetrip.backend.domain.repository.*;
import com.safetrip.backend.infrastructure.integration.zurich.client.PaymentOrderClient;
import com.safetrip.backend.infrastructure.integration.zurich.dto.request.CustomPaymentRequest;
import com.safetrip.backend.infrastructure.integration.zurich.dto.response.CustomPaymentResponse;
import com.safetrip.backend.infrastructure.integration.zurich.dto.response.CustomStatusPaymentResponse;
import com.safetrip.backend.infrastructure.integration.zurich.dto.response.StatusPaymentResponse;
import com.safetrip.backend.web.dto.request.PurchaseTimePlanRequest;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletTimePlanPurchaseServiceImpl implements WalletTimePlanPurchaseService {

    private final PolicyPlanRepository policyPlanRepository;
    private final WalletRepository walletRepository;
    private final WalletTypeRepository walletTypeRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentTypeRepository paymentTypeRepository;
    private final PaymentOrderClient paymentOrderClient;
    private final PolicyRepository policyRepository;
    private final PolicyTypeRepository policyTypeRepository;
    private final PolicyPaymentRepository policyPaymentRepository;
    private final PolicyDetailRepository policyDetailRepository;
    private final AsyncPaymentTaskService asyncPaymentTaskService;

    private static final Long WALLET_TYPE_TIME = 2L;
    private static final Long POLICY_TYPE_INNOMINADA = 3L;

    @Value("${zurich.confirmation-url.custom-sh}")
    private String confirmationUrl;

    @Override
    @Transactional
    public PurchasePlanResponse purchaseTimePlan(PurchaseTimePlanRequest request) {
        log.info("🕒 Iniciando compra de plan por tiempo: Plan ID {}, Habitaciones: {}",
                request.getPolicyPlanId(), request.getRooms());

        User currentUser = getAuthenticatedUser();

        // Validar si ya tiene una wallet activa
        validateNoActiveWallet(currentUser);

        // 1. Obtener el plan y validar
        PolicyPlan plan = policyPlanRepository.findById(request.getPolicyPlanId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Plan no encontrado: " + request.getPolicyPlanId()));

        if (!plan.getActive()) {
            throw new IllegalArgumentException("El plan seleccionado no está activo");
        }

        int days = plan.getPax();

        // 2. Calcular el monto
        BigDecimal basePrice = plan.getPolicyType().getBaseValue();
        int roomCount = request.getRooms();

        BigDecimal discountOcupational = getOccupationalDiscount(days);
        BigDecimal discountCommercial = plan.getDiscountPercentage();

        BigDecimal subtotal = basePrice
                .multiply(BigDecimal.valueOf(roomCount))
                .multiply(BigDecimal.valueOf(days));

        BigDecimal totalAmount = subtotal
                .multiply(BigDecimal.ONE.subtract(discountOcupational.divide(BigDecimal.valueOf(100))))
                .multiply(BigDecimal.ONE.subtract(discountCommercial.divide(BigDecimal.valueOf(100))))
                .setScale(0, RoundingMode.HALF_UP);

        log.info("💰 Total calculado: ${}", totalAmount);

        // 3. Calcular fechas
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days - 1);

        ZonedDateTime departureDateTime = startDate.atStartOfDay(ZonedDateTime.now().getZone());
        ZonedDateTime arrivalDateTime = endDate.atTime(23, 59, 59).atZone(ZonedDateTime.now().getZone());

        log.info("📅 Vigencia del plan ({} días): {} hasta {} (inclusivo)",
                days, startDate, endDate);

        // 4. Crear pre-wallet
        WalletType walletType = walletTypeRepository.findById(WALLET_TYPE_TIME)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de wallet TIEMPO no encontrado"));

        Wallet preWallet = new Wallet(
                null,
                walletType,
                currentUser,
                BigDecimal.valueOf(request.getRooms()),
                ZonedDateTime.now(),
                ZonedDateTime.now(),
                departureDateTime,
                arrivalDateTime,
                null
        );

        Wallet savedPreWallet = walletRepository.save(preWallet);
        log.info("💼 PRE-WALLET creada: ID {} para {} habitaciones",
                savedPreWallet.getWalletId(), request.getRooms());

        // 5. Crear pre-póliza inominada
        PolicyType policyTypeInnominada = policyTypeRepository.findById(POLICY_TYPE_INNOMINADA)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tipo de póliza INOMINADA no encontrado (ID: 3)"));

        Policy prePolicy = new Policy(
                null,
                policyTypeInnominada,
                request.getRooms(),
                totalAmount,
                null,
                null,
                currentUser,
                ZonedDateTime.now(),
                ZonedDateTime.now(),
                false,
                null,
                null
        );

        Policy savedPrePolicy = policyRepository.save(prePolicy);
        log.info("📋 PRE-PÓLIZA INOMINADA creada: ID {} (tipo 3)", savedPrePolicy.getPolicyId());

        // 6. Crear PolicyDetail
        PolicyDetail policyDetail = new PolicyDetail(
                null,
                savedPrePolicy,
                "Colombia",
                "Colombia",
                departureDateTime,
                arrivalDateTime,
                null,
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );

        policyDetailRepository.save(policyDetail);
        log.info("📝 PolicyDetail creado con vigencia: {} - {}",
                departureDateTime.toLocalDate(), arrivalDateTime.toLocalDate());

        // 7. Generar link de pago
        CustomPaymentRequest customPaymentRequest = new CustomPaymentRequest();

        String formattedAmount = totalAmount.toString();

        customPaymentRequest.setAmount(formattedAmount);
        customPaymentRequest.setPlanName(String.format(
                "Plan %s - %d habitaciones",
                getPlanTypeName(days),
                request.getRooms()
        ));
        customPaymentRequest.setCurrency("COP");
        customPaymentRequest.setHotelName(currentUser.getPerson().getFullName());
        customPaymentRequest.setNit(currentUser.getPerson().getDocumentType() +
                currentUser.getPerson().getDocumentNumber());
        customPaymentRequest.setConfirmationUrl(confirmationUrl);
        customPaymentRequest.setStartDate(startDate);
        customPaymentRequest.setEndDate(endDate);

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

        // 8. Crear Payment PENDING
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

        // 9. Crear PolicyPayment
        PolicyPayment policyPayment = new PolicyPayment(
                null,
                savedPayment,
                savedPrePolicy,
                totalAmount,
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );

        policyPaymentRepository.save(policyPayment);
        log.info("📝 PolicyPayment creado para pre-póliza {}", savedPrePolicy.getPolicyId());

        // 10. Crear WalletTransaction
        WalletTransaction transaction = new WalletTransaction(
                null,
                savedPayment,
                savedPreWallet,
                true,
                BigDecimal.valueOf(request.getRooms()),
                BigDecimal.ZERO,
                String.format("Compra de plan %s - %d habitaciones con %s%% descuento ocupacional y %s%% fidelidad",
                        getPlanTypeName(days), request.getRooms(), discountOcupational, plan.getDiscountPercentage()),
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );

        WalletTransaction savedTransaction = walletTransactionRepository.save(transaction);
        log.info("📝 WalletTransaction creada: ID {}", savedTransaction.getWalletTransactionId());

        log.info("✅ Compra de plan por tiempo procesada exitosamente");

        return PurchasePlanResponse.builder()
                .paymentUrl(paymentLink)
                .walletId(savedPreWallet.getWalletId())
                .paymentId(savedPayment.getPaymentId())
                .totalAmount(totalAmount)
                .message("Link de pago generado exitosamente")
                .build();
    }

    private void validateNoActiveWallet(User user) {
        ZonedDateTime now = ZonedDateTime.now();

        List<Wallet> activeWallets = walletRepository.findByUserAndWalletType(
                        user.getUserId(), WALLET_TYPE_TIME
                ).stream()
                .filter(w -> w.getTransactionId() != null)
                .filter(w -> w.getEndDate() != null && w.getEndDate().isAfter(now))
                .toList();

        if (!activeWallets.isEmpty()) {
            Wallet activeWallet = activeWallets.get(0);
            String endDateFormatted = activeWallet.getEndDate().toLocalDate().toString();

            log.warn("🚫 Usuario {} ya tiene una wallet activa (ID: {}) hasta {}",
                    user.getUserId(), activeWallet.getWalletId(), endDateFormatted);

            throw new IllegalStateException(
                    String.format("Ya tienes un plan activo vigente hasta el %s. " +
                                    "No puedes comprar un nuevo plan hasta que finalice el actual.",
                            endDateFormatted)
            );
        }

        log.info("✅ Usuario {} no tiene wallets activas. Puede proceder con la compra.",
                user.getUserId());
    }

    @Override
    @Transactional
    public void confirmTimePlanPayment(String voucher, String statusCard) throws RuntimeException {
        log.info("🔔 Confirmando pago de plan por tiempo - Voucher: {}", voucher);

        // 1. Buscar el pago por voucher
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
                processSuccessfulPayment(payment, walletTransaction, voucher);
                break;

            case "Pendiente":
                log.info("⏳ Pago aún pendiente - No se actualizará el estado");
                // No hacer nada, mantener el estado actual
                return;

            case "Fallida":
            case "Abandonada":
            case "Cancelada":
            case "Rechazada":
                log.error("❌ Pago no exitoso - Status: {}, Descripción: {}",
                        paymentStatus, statusResponse.getPaymentDescription());
                processFailedPayment(payment, paymentStatus, statusResponse.getPaymentDescription());
                throw new RuntimeException(
                        String.format("El pago no fue aprobado. Estado: %s - %s",
                                paymentStatus, statusResponse.getPaymentDescription())
                );

            default:
                log.warn("⚠️ Estado de pago desconocido: {}", paymentStatus);
                throw new RuntimeException("Estado de pago no reconocido: " + paymentStatus);
        }
    }

    private void processSuccessfulPayment(Payment payment, WalletTransaction walletTransaction, String voucher) {
        ZonedDateTime now = ZonedDateTime.now();

        // 1. Actualizar estado del pago a COMPLETED
        int paymentUpdated = paymentRepository.updateStatus(
                payment.getPaymentId(),
                PaymentStatus.COMPLETED,
                now
        );

        if (paymentUpdated == 0) {
            throw new RuntimeException("No se pudo actualizar el estado del pago");
        }

        Wallet wallet = walletTransaction.getWallet();

        // 2. Actualizar wallet con transactionId
        int walletUpdated = walletRepository.updateTransactionId(
                wallet.getWalletId(),
                voucher,
                now
        );

        if (walletUpdated == 0) {
            throw new RuntimeException("No se pudo actualizar la wallet con el voucher");
        }

        walletTransactionRepository.updateTransactionOnConfirm(
                walletTransaction.getWalletTransactionId(),
                walletTransaction.getTotal(),
                "Pago confirmado - Voucher: " + voucher,
                now
        );

        log.info("✅ Wallet {} actualizada con transactionId: {}", wallet.getWalletId(), voucher);

        // 3. Actualizar póliza inominada
        PolicyPayment policyPayment = policyPaymentRepository.findByPayment(payment)
                .orElseThrow(() -> new RuntimeException("No se encontró pago de póliza"));

        Policy policy = policyPayment.getPolicy();

        // Generar número de póliza
        String policyNumber = voucher + "-001";

        int policyUpdated = policyRepository.patchPolicy(
                policy.getPolicyId(),
                policyNumber,
                now,
                policy.getUnitPriceWithDiscount(),
                policy.getPersonCount()
        );

        if (policyUpdated == 0) {
            throw new RuntimeException("No se pudo actualizar la póliza con el número");
        }

        log.info("✅ Póliza {} actualizada con número: {}", policy.getPolicyId(), policyNumber);

        // 4. Ejecutar tareas post-pago
        asyncPaymentTaskService.executePostPaymentTasks(policy.getPolicyId(), payment.getPaymentId());

        log.info("✅ Pago de plan por tiempo confirmado completamente. " +
                "Wallet: {}, Póliza: {}", wallet.getWalletId(), policy.getPolicyId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void processFailedPayment(Payment payment, String paymentStatus, String paymentDescription) {
        ZonedDateTime now = ZonedDateTime.now();

        // Actualizar estado del pago a FAILED
        int paymentUpdated = paymentRepository.updateStatus(
                payment.getPaymentId(),
                PaymentStatus.FAILED,
                now
        );

        if (paymentUpdated == 0) {
            throw new RuntimeException("No se pudo actualizar el estado del pago a FAILED");
        }

        log.info("❌ Pago marcado como FAILED - Status: {}, Descripción: {}",
                paymentStatus, paymentDescription);
    }

    private BigDecimal getOccupationalDiscount(int days) {
        if (days == 30) {
            return BigDecimal.valueOf(40);
        } else if (days == 182 || days == 183) {
            return BigDecimal.valueOf(45);
        } else if (days == 365 || days == 366) {
            return BigDecimal.valueOf(50);
        }
        return BigDecimal.valueOf(40);
    }

    private String getPlanTypeName(int days) {
        if (days == 30) return "Mensual";
        if (days == 182 || days == 183) return "Semestral";
        if (days == 365 || days == 366) return "Anual";
        return days + " días";
    }

    private User getAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}