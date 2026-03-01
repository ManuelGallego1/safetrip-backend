package com.safetrip.backend.infrastructure.integration.pdf.mapper;

import com.safetrip.backend.domain.model.*;
import com.safetrip.backend.infrastructure.integration.notification.resend.dto.EmailAttachment;
import com.safetrip.backend.infrastructure.integration.pdf.dto.Template02ApData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class Template02ApMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public Template02ApData toTemplate02ApData(
            Policy policy,
            PolicyDetail policyDetail,
            PolicyPayment policyPayment,
            List<PolicyPerson> policyPersons,
            List<EmailAttachment> attachments) {

        boolean hasAttachments = attachments != null && !attachments.isEmpty();
        boolean hasPersons = policyPersons != null && !policyPersons.isEmpty();

        log.debug("📊 Mapeando Template02ApData para póliza {}: archivos={}, personas={}",
                policy.getPolicyId(), hasAttachments, hasPersons);

        if (hasAttachments && hasPersons) {
            log.warn("⚠️ La póliza {} tiene AMBOS archivos y personas. Priorizando modo archivos.",
                    policy.getPolicyId());
        }

        String formattedValue = formatMoney(
                policyPayment != null && policyPayment.getAppliedAmount() != null
                        ? policyPayment.getAppliedAmount()
                        : BigDecimal.ZERO
        );

        String policyNumber = policy.getPolicyNumber() != null
                ? policy.getPolicyNumber()
                : "PENDIENTE";

        Long tipo = policy.getPolicyType() != null
                ? policy.getPolicyType().getPolicyTypeId()
                : null;

        // 🆕 CALCULAR PLAN PARA TIPO 3
        String plan = null;
        if (tipo != null && tipo == 3L) {
            plan = calculatePlanName(policyDetail.getDeparture(), policyDetail.getArrival());
            log.debug("✅ Plan calculado para tipo 3: {}", plan);
        }

        if (hasAttachments) {
            log.info("📎 Modo archivos adjuntos: {} archivo(s) para póliza {}",
                    attachments.size(), policyNumber);

            return Template02ApData.builder()
                    .tipo(tipo)
                    .numPoliza(policyNumber)
                    .valor(formattedValue)
                    .plan(plan)
                    .createdWithFiles(true)
                    .attachedFiles(attachments)
                    .nombres(Collections.emptyList())
                    .documentos(Collections.emptyList())
                    .fechasDesde(Collections.emptyList())
                    .fechasHasta(Collections.emptyList())
                    .build();
        }

        if (hasPersons) {
            log.info("👥 Modo lista de asegurados: {} persona(s) para póliza {}",
                    policyPersons.size(), policyNumber);

            String fechaDesde = policyDetail.getDeparture() != null
                    ? policyDetail.getDeparture().format(DATE_FORMATTER)
                    : "N/A";

            String fechaHasta = policyDetail.getArrival() != null
                    ? policyDetail.getArrival().format(DATE_FORMATTER)
                    : "N/A";

            List<String> nombres = new ArrayList<>();
            List<String> documentos = new ArrayList<>();
            List<String> fechasDesde = new ArrayList<>();
            List<String> fechasHasta = new ArrayList<>();

            for (int i = 0; i < policyPersons.size(); i++) {
                PolicyPerson pp = policyPersons.get(i);
                Person person = pp.getPerson();

                nombres.add(person.getFullName());
                documentos.add(person.getDocumentType().getCode() + " " + person.getDocumentNumber());
                fechasDesde.add(fechaDesde);
                fechasHasta.add(fechaHasta);

                if (i < policyPersons.size() - 1) {
                    nombres.add("");
                    documentos.add("");
                    fechasDesde.add("");
                    fechasHasta.add("");
                }
            }

            return Template02ApData.builder()
                    .tipo(tipo)
                    .numPoliza(policyNumber)
                    .valor(formattedValue)
                    .plan(plan)
                    .createdWithFiles(false)
                    .attachedFiles(Collections.emptyList())
                    .nombres(nombres)
                    .documentos(documentos)
                    .fechasDesde(fechasDesde)
                    .fechasHasta(fechasHasta)
                    .build();
        }

        log.warn("⚠️ La póliza {} no tiene ni archivos ni personas aseguradas", policyNumber);

        return Template02ApData.builder()
                .tipo(tipo)
                .numPoliza(policyNumber)
                .valor(formattedValue)
                .plan(plan)
                .createdWithFiles(false)
                .attachedFiles(Collections.emptyList())
                .nombres(Collections.emptyList())
                .documentos(Collections.emptyList())
                .fechasDesde(Collections.emptyList())
                .fechasHasta(Collections.emptyList())
                .build();
    }

    private String calculatePlanName(ZonedDateTime startDate, ZonedDateTime endDate) {
        if (startDate == null || endDate == null) {
            log.warn("⚠️ Fechas de póliza null, usando plan por defecto");
            return "Plan No Disponible";
        }

        long days = ChronoUnit.DAYS.between(startDate.toLocalDate(), endDate.toLocalDate());

        log.debug("📅 Calculando plan: {} días de diferencia", days);

        if (days >= 28 && days <= 32) {
            return "Plan Mensual";
        } else if (days >= 180 && days <= 185) {
            return "Plan Semestral";
        } else if (days >= 363 && days <= 368) {
            return "Plan Anual";
        } else {
            log.warn("⚠️ Duración no estándar: {} días", days);
            return "Plan " + days + " días";
        }
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) {
            return "0";
        }
        long longValue = value.longValue();
        return String.format("%,d", longValue).replace(",", ".");
    }
}