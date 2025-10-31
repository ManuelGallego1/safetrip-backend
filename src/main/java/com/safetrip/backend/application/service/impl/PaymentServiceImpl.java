package com.safetrip.backend.application.service.impl;

import com.safetrip.backend.application.dto.PaymentDTO;
import com.safetrip.backend.application.service.PaymentService;
import com.safetrip.backend.domain.model.*;
import com.safetrip.backend.domain.model.enums.PaymentStatus;
import com.safetrip.backend.domain.repository.*;
import com.safetrip.backend.infrastructure.integration.zurich.client.PaymentOrderClient;
import com.safetrip.backend.infrastructure.integration.zurich.dto.request.AddOrderPaymentRequest;
import com.safetrip.backend.infrastructure.integration.zurich.dto.request.PassengerRequest;
import com.safetrip.backend.infrastructure.integration.zurich.dto.response.AddOrderPaymentResponse;
import com.safetrip.backend.infrastructure.integration.zurich.dto.response.LinkCobroResponse;
import com.safetrip.backend.infrastructure.integration.zurich.dto.response.StatusPaymentResponse;
import com.safetrip.backend.infrastructure.persistence.entity.PolicyEntity;
import com.safetrip.backend.infrastructure.persistence.entity.PolicyPaymentEntity;
import com.safetrip.backend.infrastructure.persistence.mapper.PolicyMapper;
import com.safetrip.backend.web.dto.mapper.PolicyResponseMapper;
import com.safetrip.backend.web.dto.request.ConfirmPaymentRequest;
import com.safetrip.backend.web.dto.response.PolicyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentOrderClient paymentOrderClient;
    private final PaymentTypeRepository paymentTypeRepository;
    private final PaymentRepository paymentRepository;
    private final PolicyRepository policyRepository;
    private final PolicyPaymentRepository policyPaymentRepository;
    private final PolicyResponseMapper policyResponseMapper;


    public PaymentServiceImpl(PaymentOrderClient paymentOrderClient,
                              PaymentTypeRepository paymentTypeRepository,
                              PaymentRepository paymentRepository,
                              PolicyRepository policyRepository,
                              PolicyPaymentRepository policyPaymentRepository,
                              PolicyResponseMapper policyResponseMapper) {
        this.paymentOrderClient = paymentOrderClient;
        this.paymentTypeRepository = paymentTypeRepository;
        this.paymentRepository = paymentRepository;
        this.policyRepository = policyRepository;
        this.policyPaymentRepository = policyPaymentRepository;
        this.policyResponseMapper = policyResponseMapper;
    }

    private User getAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);


    @Override
    public String createdPaymentWithZurich(PaymentDTO paymentDTO) {
        User createdBy = getAuthenticatedUser();

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
            pr.setApellidoPax(lastName);
            pr.setFechaNacimientoPax("1999-08-24");
            pr.setDocumentPax(p.getDocumentNumber() != null ? p.getDocumentNumber() : null);
            pr.setCorreoPax(createdBy.getEmail());
            pr.setMensajeCondicionesMedicas("N/A");
            return pr;
        }).collect(Collectors.toList());

        Integer selectedPlanId = null;
        if (paymentDTO.getPolicyTypeId() != null) {
            if (paymentDTO.getPolicyTypeId() == 1L) {
                selectedPlanId = 2240;
            } else if (paymentDTO.getPolicyTypeId() == 2L) {
                selectedPlanId = 2245;
            }
        }

        var departure = paymentDTO.getDeparture();
        var arrival = paymentDTO.getArrival();

        if (departure.toLocalDate().isEqual(arrival.toLocalDate())) {
            departure = departure.toLocalDate().atStartOfDay(paymentDTO.getDeparture().getZone());
            arrival = departure.plusHours(24).minusSeconds(1);
        }

        AddOrderPaymentRequest request = new AddOrderPaymentRequest();
        request.setInfoPasajeros(passengers);
        request.setCosto(0.00);
        request.setFechaSalida(departure.toLocalDate());
        request.setFechaLlegada(arrival.toLocalDate());
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

        AddOrderPaymentResponse orderResponse = paymentOrderClient.sendOrder(request);

        Policy policy = policyRepository.findById(paymentDTO.getPolicyId())
                .orElseThrow(() -> new RuntimeException("Error en poliza"));

        if (orderResponse == null || orderResponse.getCodigo() == null) {
            throw new RuntimeException("No se pudo generar el voucher de pago");
        }

        LinkCobroResponse linkResponse = paymentOrderClient.getLinkCobroVoucher(orderResponse.getCodigo());

        PaymentType paymentType = paymentTypeRepository.findById(paymentDTO.getPaymentTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Payment type not found: " + paymentDTO.getPaymentTypeId()));

        BigDecimal amount = BigDecimal.valueOf(linkResponse.getAmount());

        Payment payment = new Payment(
                null,
                paymentType,
                PaymentStatus.PENDING,
                orderResponse.getCodigo(),
                amount,
                createdBy,
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );

        Payment savedPayment = paymentRepository.save(payment);

        PolicyPayment policyPayment = new PolicyPayment(
                null,
                savedPayment,
                policy,
                amount,
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );

        policyPaymentRepository.save(policyPayment);

        if (linkResponse == null || linkResponse.getLink() == null) {
            throw new RuntimeException("No se pudo obtener el link de pago");
        }

        return linkResponse.getLink();
    }

    @Override
    public String cretaedPaymentWithWallet(PaymentDTO paymentDTO) {
        return "";
    }

    @Override
    @Transactional
    public PolicyResponse confirmPayment(ConfirmPaymentRequest request) {
        // 1. Buscar el pago por voucher primero
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
            String errorMsg = String.format(
                    "El pago no puede ser confirmado. Estado actual: %s (%s)",
                    statusResponse.getDescStatus(),
                    statusResponse.getStatus()
            );

            log.error("❌ {}", errorMsg);
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

        // 8. Retornar póliza actualizada
        Policy updatedPolicy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Póliza no encontrada"));

        return policyResponseMapper.toDto(updatedPolicy);
    }
}