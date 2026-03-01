package com.safetrip.backend.application.service.impl;

import com.safetrip.backend.application.dto.PaymentDTO;
import com.safetrip.backend.application.service.PaymentService;
import com.safetrip.backend.application.usecase.GeneratePolicyPdfUseCase;
import com.safetrip.backend.domain.model.*;
import com.safetrip.backend.domain.model.enums.PaymentStatus;
import com.safetrip.backend.domain.repository.*;
import com.safetrip.backend.infrastructure.integration.notification.resend.client.EmailClient;
import com.safetrip.backend.infrastructure.integration.zurich.client.PaymentOrderClient;
import com.safetrip.backend.infrastructure.integration.zurich.dto.request.AddOrderPaymentRequest;
import com.safetrip.backend.infrastructure.integration.zurich.dto.request.PassengerRequest;
import com.safetrip.backend.infrastructure.integration.zurich.dto.response.AddOrderPaymentResponse;
import com.safetrip.backend.infrastructure.integration.zurich.dto.response.LinkCobroResponse;
import com.safetrip.backend.infrastructure.integration.zurich.dto.response.StatusPaymentResponse;
import com.safetrip.backend.web.dto.mapper.PolicyResponseMapper;
import com.safetrip.backend.web.dto.request.ConfirmPaymentRequest;
import com.safetrip.backend.web.dto.response.ApiResponse;
import com.safetrip.backend.web.dto.response.PaymentDetailsResponse;
import com.safetrip.backend.web.dto.response.PolicyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentOrderClient paymentOrderClient;
    private final PaymentTypeRepository paymentTypeRepository;
    private final PaymentRepository paymentRepository;
    private final PolicyRepository policyRepository;
    private final PolicyPaymentRepository policyPaymentRepository;
    private final PolicyResponseMapper policyResponseMapper;
    private final AsyncPaymentTaskService asyncPaymentTaskService;

    public PaymentServiceImpl(PaymentOrderClient paymentOrderClient,
                              PaymentTypeRepository paymentTypeRepository,
                              PaymentRepository paymentRepository,
                              PolicyRepository policyRepository,
                              PolicyPaymentRepository policyPaymentRepository,
                              PolicyResponseMapper policyResponseMapper,
                              AsyncPaymentTaskService asyncPaymentTaskService) {
        this.paymentOrderClient = paymentOrderClient;
        this.paymentTypeRepository = paymentTypeRepository;
        this.paymentRepository = paymentRepository;
        this.policyRepository = policyRepository;
        this.policyPaymentRepository = policyPaymentRepository;
        this.policyResponseMapper = policyResponseMapper;
        this.asyncPaymentTaskService = asyncPaymentTaskService;
    }
                                                          
    private User getAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    @Transactional
    public String createdPaymentWithZurich(PaymentDTO paymentDTO) {
        User createdBy = getAuthenticatedUser();
        ZoneId colombiaZone = ZoneId.of("America/Bogota");

        // Preparar lista de pasajeros
        List<PassengerRequest> passengers = paymentDTO.getPersons().stream().map(p -> {
            PassengerRequest pr = new PassengerRequest();

            String fullName = p.getFullName() != null ? p.getFullName().trim() : "";
            String firstName = fullName;
            String lastName = "";
            int spaceIndex = fullName.indexOf(" ");
            if (spaceIndex > 0) {
                firstName = fullName.substring(0, spaceIndex);
                lastName = fullName.substring(spaceIndex + 1);
            }

            pr.setNombrePax(firstName);
            pr.setApellidoPax(lastName.isEmpty() ? "Pyme" : lastName);
            pr.setFechaNacimientoPax("1999-08-24");
            pr.setDocumentPax(p.getDocumentNumber() != null ? p.getDocumentNumber() : null);
            pr.setCorreoPax(createdBy.getEmail());
            pr.setMensajeCondicionesMedicas("N/A");
            return pr;
        }).collect(Collectors.toList());

        // Determinar plan según tipo de póliza
        Integer selectedPlanId = null;
        if (paymentDTO.getPolicyTypeId() != null) {
            if (paymentDTO.getPolicyTypeId() == 1L) {
                selectedPlanId = 2240;
            } else if (paymentDTO.getPolicyTypeId() == 2L) {
                selectedPlanId = 2245;
            }
        }

        // Procesar fechas con zona horaria de Colombia
        ZonedDateTime departureInColombia = paymentDTO.getDeparture()
                .withZoneSameInstant(colombiaZone);
        ZonedDateTime arrivalInColombia = paymentDTO.getArrival()
                .withZoneSameInstant(colombiaZone);

        LocalDate departureDate = departureInColombia.toLocalDate();
        LocalDate arrivalDate = arrivalInColombia.toLocalDate();

        log.info("📅 Fechas procesadas - Salida: {}, Llegada: {}", departureDate, arrivalDate);

        // Crear request de orden de pago
        AddOrderPaymentRequest request = new AddOrderPaymentRequest();
        request.setInfoPasajeros(passengers);
        request.setCosto(0.00);
        request.setFechaSalida(departureDate);
        request.setFechaLlegada(arrivalDate);
        request.setReferencia("N/A");
        request.setMoneda("COP");
        request.setPasajeros(passengers.size());
        request.setNombreContacto(createdBy.getPerson().getFullName());
        request.setTelefonoContacto(createdBy.getPhone());
        request.setEmailContacto(createdBy.getEmail());
        request.setConsideracionesGenerales("N/A");
        request.setEmision(1);
        request.setPlan(selectedPlanId);
        request.setPaisDestino(1);
        request.setPaisOrigen(47);
        request.setTasaCambio(1);
        request.setUpgrades(Collections.emptyList());

        // Enviar orden y obtener respuesta
        AddOrderPaymentResponse orderResponse = paymentOrderClient.sendOrder(request);
        if (orderResponse == null || orderResponse.getCodigo() == null) {
            throw new RuntimeException("No se pudo generar el voucher de pago");
        }

        // Obtener link de cobro
        LinkCobroResponse linkResponse = paymentOrderClient.getLinkCobroVoucher(orderResponse.getCodigo());
        if (linkResponse == null || linkResponse.getLink() == null) {
            throw new RuntimeException("No se pudo obtener el link de pago");
        }

        // Buscar póliza y tipo de pago
        Policy policy = policyRepository.findById(paymentDTO.getPolicyId())
                .orElseThrow(() -> new RuntimeException("Error en poliza"));

        PaymentType paymentType = paymentTypeRepository.findById(paymentDTO.getPaymentTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Payment type not found: " + paymentDTO.getPaymentTypeId()));

        // Crear y guardar pago
        BigDecimal amount = BigDecimal.valueOf(linkResponse.getAmount());

        Payment payment = new Payment(
                null,
                paymentType,
                PaymentStatus.PENDING,
                orderResponse.getCodigo(),
                amount,
                createdBy,
                ZonedDateTime.now(colombiaZone),
                ZonedDateTime.now(colombiaZone)
        );

        Payment savedPayment = paymentRepository.save(payment);

        // Crear y guardar relación póliza-pago
        PolicyPayment policyPayment = new PolicyPayment(
                null,
                savedPayment,
                policy,
                amount,
                ZonedDateTime.now(colombiaZone),
                ZonedDateTime.now(colombiaZone)
        );

        policyPaymentRepository.save(policyPayment);

        return linkResponse.getLink();
    }

    @Override
    public String cretaedPaymentWithWallet(PaymentDTO paymentDTO) {
        return "";
    }

    @Override
    @Transactional(noRollbackFor = RuntimeException.class)
    public PolicyResponse confirmPayment(ConfirmPaymentRequest request) {
        // 1. Buscar el pago por voucher
        Payment payment = paymentRepository
                .findByTransactionId(request.getVoucher())
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con voucher: " + request.getVoucher()));

        // 2. Verificar si ya fue confirmado (idempotencia)
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            log.info("ℹ️ El pago ya fue confirmado anteriormente (idempotencia)");

            PolicyPayment policyPayment = policyPaymentRepository
                    .findByPayment(payment)
                    .orElseThrow(() -> new RuntimeException("No se encontró pago de póliza"));

            Policy policy = policyRepository.findById(policyPayment.getPolicy().getPolicyId())
                    .orElseThrow(() -> new RuntimeException("Póliza no encontrada"));

            return policyResponseMapper.toDto(policy);
        }

        // 3. Verificar estado del pago en Zurich
        StatusPaymentResponse statusResponse = paymentOrderClient.getStatusPayment(request.getVoucher());

        log.info("📋 Estado del pago en Zurich - Status: {}, Descripción: {}",
                statusResponse.getStatus(), statusResponse.getDescStatus());

        // 4. Validar que el pago esté activo (status = "1")
        if (!"1".equals(statusResponse.getStatus())) {
            handleFailedPaymentStatus(payment, statusResponse);
        }

        log.info("✅ Pago verificado exitosamente en Zurich - Estado: Activo");

        // 5. Buscar el pago de póliza asociado
        PolicyPayment policyPayment = policyPaymentRepository
                .findByPayment(payment)
                .orElseThrow(() -> new RuntimeException("No se encontró pago de póliza"));

        Long policyId = policyPayment.getPolicy().getPolicyId();

        // 6. Actualizar estado del pago a COMPLETED
        int updated = paymentRepository.updateStatus(
                payment.getPaymentId(),
                PaymentStatus.COMPLETED,
                ZonedDateTime.now()
        );

        if (updated == 0) {
            throw new RuntimeException("No se pudo actualizar el estado del pago");
        }

        log.info("✅ Estado del pago actualizado a COMPLETED");

        // 7. Actualizar póliza con número de voucher y monto total
        int updatedRows = policyRepository.patchPolicy(
                policyId,
                request.getVoucher(),
                ZonedDateTime.now(),
                statusResponse.getTotal(),
                null
        );

        if (updatedRows == 0) {
            throw new RuntimeException("No se pudo actualizar la póliza");
        }

        log.info("✅ Póliza {} actualizada con voucher {} y monto {}",
                policyId, request.getVoucher(), statusResponse.getTotal());

        // 8. Obtener póliza actualizada para retornar
        Policy updatedPolicy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Póliza no encontrada"));

        // 9. Ejecutar tareas post-pago de forma asíncrona
        // Delegar al servicio asíncrono que maneja su propia transacción
        asyncPaymentTaskService.executePostPaymentTasks(policyId, payment.getPaymentId());

        return policyResponseMapper.toDto(updatedPolicy);
    }

    /**
     * Maneja los estados de pago fallidos
     */
    private void handleFailedPaymentStatus(Payment payment, StatusPaymentResponse statusResponse) {
        String errorMsg = String.format(
                "El pago no puede ser confirmado. Estado actual: %s (%s)",
                statusResponse.getDescStatus(),
                statusResponse.getStatus()
        );

        log.error("❌ {}", errorMsg);

        // Solo actualizamos si NO es pendiente (estado "5")
        if (!"5".equals(statusResponse.getStatus())) {
            paymentRepository.updateStatus(
                    payment.getPaymentId(),
                    PaymentStatus.FAILED,
                    ZonedDateTime.now()
            );

            log.warn("⚠️ Pago {} marcado como FAILED", payment.getPaymentId());
        }

        // Mapear estados de Zurich y lanzar excepción específica
        switch (statusResponse.getStatus()) {
            case "2":
                throw new RuntimeException("El pago ha sido anulado y no puede ser procesado");
            case "4":
                throw new RuntimeException("El pago ha expirado");
            case "5":
                throw new RuntimeException("El pago aún está pendiente de confirmación");
            default:
                throw new RuntimeException(errorMsg);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<PaymentDetailsResponse>> getUserPayments() {
        User currentUser = getAuthenticatedUser();

        log.info("🔍 Obteniendo pagos para el usuario: {} (ID: {}) - Rol: {}",
                currentUser.getEmail(), currentUser.getUserId(), currentUser.getRole().getName());

        try {
            List<Payment> payments;

            // ✅ Si es SUPER_ADMIN (roleId = 1), traer todos los pagos
            if (currentUser.getRole().getRoleId().equals(1L)) {
                log.info("👑 Usuario SUPER_ADMIN detectado - Obteniendo TODOS los pagos del sistema");
                payments = paymentRepository.findAll();
            } else {
                // Usuario normal - solo sus pagos
                payments = paymentRepository.findByUserId(currentUser.getUserId());
            }

            log.info("✅ Se encontraron {} pagos", payments.size());

            List<PaymentDetailsResponse> paymentDetails = payments.stream()
                    .sorted(Comparator.comparing(Payment::getCreatedAt).reversed())
                    .map(payment -> {
                        Optional<PolicyPayment> policyPayment = policyPaymentRepository.findByPayment(payment);

                        PaymentDetailsResponse response = new PaymentDetailsResponse();
                        response.setId(payment.getPaymentId());
                        response.setTransactionId(payment.getTransactionId());
                        response.setAmount(payment.getAmount());
                        response.setStatus(payment.getStatus());
                        response.setDate(payment.getCreatedAt());
                        response.setPaymentTypeName(payment.getPaymentType().getName());

                        // ✅ OPCIONAL: Agregar info del usuario dueño del pago (útil para admin)
                        if (currentUser.getRole().getRoleId().equals(1L)) {
                            response.setUserEmail(payment.getUser().getEmail());
                            response.setUserName(payment.getUser().getPerson().getFullName());
                        }

                        return response;
                    })
                    .collect(Collectors.toList());

            return ApiResponse.success(
                    currentUser.getRole().getRoleId().equals(1L)
                            ? "Todos los pagos obtenidos exitosamente"
                            : "Pagos obtenidos exitosamente",
                    paymentDetails
            );

        } catch (Exception e) {
            log.error("❌ Error obteniendo pagos del usuario {}: {}",
                    currentUser.getUserId(), e.getMessage(), e);
            throw new RuntimeException("Error al obtener los pagos del usuario", e);
        }
    }
}