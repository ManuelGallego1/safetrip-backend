package com.safetrip.backend.infrastructure.integration.pdf.mapper;

import com.safetrip.backend.domain.model.*;
import com.safetrip.backend.domain.model.enums.RelationshipType;
import com.safetrip.backend.infrastructure.integration.pdf.dto.Template01ApData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
public class Template01ApMapper {

    private static final String EMERGENCY_PHONE = "018000 413 033";

    /**
     * Mapea los datos de la póliza al DTO para generar el PDF Template01
     */
    public Template01ApData toTemplate01ApData(
            Policy policy,
            PolicyDetail policyDetail,
            PolicyPayment policyPayment,
            List<PolicyPerson> policyPersons,
            User user) {

        log.debug("Mapeando datos para Template01Ap - Póliza: {}", policy.getPolicyId());

        Person holderPerson = user.getPerson();

        // Calcular valor total
        BigDecimal totalValue = policyPayment != null && policyPayment.getAppliedAmount() != null
                ? policyPayment.getAppliedAmount()
                : BigDecimal.ZERO;

        // Formatear valor con separadores de miles y sin decimales
        String formattedValue = formatMoney(totalValue);

        // Extraer componentes de fechas
        ZonedDateTime createdAt = policy.getCreatedAt() != null
                ? policy.getCreatedAt()
                : ZonedDateTime.now();

        ZonedDateTime departure = policyDetail.getDeparture();
        ZonedDateTime arrival = policyDetail.getArrival() != null
                ? policyDetail.getArrival()
                : departure.plusDays(1); // Si no hay fecha de llegada, usar +1 día

        return Template01ApData.builder()
                // Número de póliza
                .numPoliza(policy.getPolicyNumber() != null
                        ? policy.getPolicyNumber()
                        : "PENDIENTE")

                // Datos del tomador
                .nomTomador(holderPerson.getFullName())
                .dir(holderPerson.getAddress() != null
                        ? holderPerson.getAddress()
                        : "N/A")
                .celular(user.getPhone() != null
                        ? user.getPhone()
                        : "N/A")

                // Documento del tomador
                .tipoDoc(holderPerson.getDocumentType().getCode())
                .numDoc(holderPerson.getDocumentNumber())

                // Número de asegurados
                .numAsegurados(String.valueOf(policy.getPersonCount()))

                // Fecha de expedición (fecha de creación)
                .hExp(String.valueOf(createdAt.getHour()))
                .diaExp(String.format("%02d", createdAt.getDayOfMonth()))
                .mesExp(getMonthName(createdAt.getMonthValue()))
                .anExp(String.valueOf(createdAt.getYear()))

                // Fecha desde (departure)
                .hDesde(String.valueOf(departure.getHour()))
                .diaDesde(String.format("%02d", departure.getDayOfMonth()))
                .mesDesde(getMonthName(departure.getMonthValue()))
                .anDesde(String.valueOf(departure.getYear()))

                // Fecha hasta (arrival)
                .diaHasta(String.format("%02d", arrival.getDayOfMonth()))
                .mesHasta(getMonthName(arrival.getMonthValue()))
                .anHasta(String.valueOf(arrival.getYear()))

                // Valor de la póliza
                .valor(formattedValue)

                // Teléfono de emergencias
                .telEmer(EMERGENCY_PHONE)

                .build();
    }

    /**
     * Obtiene el nombre del mes en español
     */
    private String getMonthName(int month) {
        String[] months = {
                "01", "02", "03", "04", "05", "06",
                "07", "08", "09", "10", "11", "12"
        };
        return months[month - 1];
    }

    /**
     * Formatea un valor monetario con separadores de miles y sin decimales
     * Ejemplo: 150000 -> "150.000"
     */
    private String formatMoney(BigDecimal value) {
        if (value == null) {
            return "0";
        }

        long longValue = value.longValue();
        return String.format("%,d", longValue).replace(",", ".");
    }
}