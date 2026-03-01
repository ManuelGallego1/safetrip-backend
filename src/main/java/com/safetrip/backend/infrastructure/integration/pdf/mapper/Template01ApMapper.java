package com.safetrip.backend.infrastructure.integration.pdf.mapper;

import com.safetrip.backend.domain.model.*;
import com.safetrip.backend.infrastructure.integration.pdf.dto.Template01ApData;
import com.safetrip.backend.infrastructure.integration.pdf.utils.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
public class Template01ApMapper {

    private static final String EMERGENCY_PHONE = "601 3570911";
    private static final int VIGENCIA_HOUR_THRESHOLD = 15; // 15:00 (3 PM)

    // Tipos de póliza
    private static final Long POLICY_TYPE_1 = 1L; // Hora por defecto: 00:00
    private static final Long POLICY_TYPE_2 = 2L; // Hora por defecto: 15:00

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

        String formattedValue = formatMoney(totalValue);

        // 🌎 FECHA DE EXPEDICIÓN (sí se convierte, porque tiene hora real)
        ZonedDateTime createdAt = policy.getCreatedAt() != null
                ? policy.getCreatedAt()
                : ZonedDateTime.now();
        ZonedDateTime createdAtColombia = DateTimeUtils.toColombiaZone(createdAt);

        // 🗓️ FECHAS DE VIAJE (departure/arrival): Usar solo la FECHA, no convertir timezone
        // Extraer solo el LocalDate y reconstruir en zona Colombia
        LocalDate departureDate = policyDetail.getDeparture().toLocalDate();
        LocalDate arrivalDate = policyDetail.getArrival() != null
                ? policyDetail.getArrival().toLocalDate()
                : departureDate.plusDays(1);

        // Reconstruir ZonedDateTime en zona Colombia con hora por defecto
        int defaultHour = POLICY_TYPE_1.equals(policy.getPolicyType().getPolicyTypeId()) ? 0 : VIGENCIA_HOUR_THRESHOLD;

        ZonedDateTime departureColombia = DateTimeUtils.createColombiaDateTime(departureDate, defaultHour, 0);
        ZonedDateTime arrivalColombia = DateTimeUtils.createColombiaDateTime(arrivalDate, 23, 59); // Fin del día

        log.debug("📅 Fecha expedición: {} -> Colombia: {}", createdAt, createdAtColombia);
        log.debug("🗓️ Fecha desde (solo fecha): {} -> Colombia: {}", departureDate, departureColombia);
        log.debug("🗓️ Fecha hasta (solo fecha): {} -> Colombia: {}", arrivalDate, arrivalColombia);

        // Verificar si la fecha de expedición y la fecha desde son el mismo día
        boolean isSameDay = createdAtColombia.toLocalDate().equals(departureDate);

        // Calcular hVigencia: true solo si mismo día Y hora >= threshold
        boolean hVigencia = isSameDay && createdAtColombia.getHour() >= VIGENCIA_HOUR_THRESHOLD;

        // Ajustar hora de inicio de vigencia
        String hDesdeAjustada;
        ZonedDateTime fechaDesdeConHora;

        if (hVigencia) {
            // Si es mismo día y >= 15:00, usar la hora real de expedición
            hDesdeAjustada = String.valueOf(createdAtColombia.getHour());
            fechaDesdeConHora = DateTimeUtils.createColombiaDateTime(
                    departureDate,
                    createdAtColombia.getHour(),
                    createdAtColombia.getMinute()
            );
        } else {
            // En cualquier otro caso, usar la hora por defecto según tipo
            hDesdeAjustada = String.valueOf(defaultHour);
            fechaDesdeConHora = departureColombia;
        }

        log.debug("⏰ Tipo: {} - Mismo día: {} - Hora exp: {}:00 - hVigencia: {} → Hora inicio: {}:00",
                policy.getPolicyType().getPolicyTypeId(), isSameDay,
                createdAtColombia.getHour(), hVigencia, hDesdeAjustada);

        return Template01ApData.builder()
                .numPoliza(policy.getPolicyNumber() != null ? policy.getPolicyNumber() : "PENDIENTE")
                .nomTomador(holderPerson.getFullName())
                .dir(holderPerson.getAddress() != null ? holderPerson.getAddress() : "N/A")
                .celular(user.getPhone() != null ? user.getPhone() : "N/A")
                .tipoDoc(holderPerson.getDocumentType().getCode())
                .numDoc(holderPerson.getDocumentNumber())
                .numAsegurados(String.valueOf(policy.getPersonCount()))

                // 🌎 ENVIAR ZONEDDATETIME YA EN ZONA COLOMBIA
                .fechaExpedicion(createdAtColombia)
                .fechaDesde(fechaDesdeConHora)
                .fechaHasta(arrivalColombia)

                // 🔙 FALLBACK: Componentes individuales
                .hExp(String.valueOf(createdAtColombia.getHour()))
                .diaExp(String.format("%02d", createdAtColombia.getDayOfMonth()))
                .mesExp(getMonthName(createdAtColombia.getMonthValue()))
                .anExp(String.valueOf(createdAtColombia.getYear()))

                .hDesde(hDesdeAjustada)
                .diaDesde(String.format("%02d", departureDate.getDayOfMonth()))
                .mesDesde(getMonthName(departureDate.getMonthValue()))
                .anDesde(String.valueOf(departureDate.getYear()))

                .diaHasta(String.format("%02d", arrivalDate.getDayOfMonth()))
                .mesHasta(getMonthName(arrivalDate.getMonthValue()))
                .anHasta(String.valueOf(arrivalDate.getYear()))

                .valor(formattedValue)
                .telEmer(EMERGENCY_PHONE)
                .tipo(policy.getPolicyType().getPolicyTypeId())
                .hVigencia(hVigencia)
                .build();
    }

    private String getMonthName(int month) {
        String[] months = {
                "01", "02", "03", "04", "05", "06",
                "07", "08", "09", "10", "11", "12"
        };
        return months[month - 1];
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) {
            return "0";
        }
        long longValue = value.longValue();
        return String.format("%,d", longValue).replace(",", ".");
    }
}