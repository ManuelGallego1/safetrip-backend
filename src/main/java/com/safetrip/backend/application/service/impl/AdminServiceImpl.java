package com.safetrip.backend.application.service.impl;

import com.safetrip.backend.application.service.AdminService;
import com.safetrip.backend.application.service.FileAppService;
import com.safetrip.backend.domain.model.*;
import com.safetrip.backend.domain.model.enums.DiscountType;
import com.safetrip.backend.domain.model.enums.PaymentStatus;
import com.safetrip.backend.domain.repository.*;
import com.safetrip.backend.web.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final PolicyRepository policyRepository;
    private final PolicyDetailRepository policyDetailRepository;
    private final PolicyPaymentRepository policyPaymentRepository;
    private final PolicyPersonRepository policyPersonRepository;
    private final FileRepository fileRepository;
    private final FileAppService fileAppService;
    private final PolicyFileRepository policyFileRepository;
    private final PaymentRepository paymentRepository;
    private final DiscountRepository discountRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<AdminSalesKPIResponse> getSalesKPIs(
            ZonedDateTime startDate, ZonedDateTime endDate) {

        log.info("📈 Admin: calculando KPIs de ventas");

        ZonedDateTime endDateUtc = endDate != null
                ? endDate
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(999_999_999)
                .withZoneSameInstant(ZoneId.of("UTC"))
                : null;


        // ✅ Revenue directo desde la DB (OPTIMIZADO)
        BigDecimal totalRevenue = startDate != null || endDateUtc != null
                ? paymentRepository.sumCompletedPaymentsAmountByDateRange(startDate, endDateUtc)
                : paymentRepository.sumAllCompletedPaymentsAmount();

        log.info("💰 Total Revenue calculado: ${}", totalRevenue);

        // ✅ Count de pagos completados desde la DB (OPTIMIZADO)
        long completedPaymentsCount = startDate != null || endDateUtc != null
                ? paymentRepository.countCompletedPaymentsByDateRange(startDate, endDateUtc)
                : paymentRepository.countAllCompletedPayments();

        log.info("💳 Pagos completados: {}", completedPaymentsCount);

        // Obtener pólizas para calcular otras métricas
        List<Policy> completedPolicies = startDate != null || endDateUtc != null
                ? policyRepository.findCompletedPoliciesByDateRange(startDate, endDateUtc)
                : policyRepository.findAllCompletedPolicies();

        long totalPersonsInsured = 0;
        long totalPaxSold = 0;
        Set<Long> uniqueCustomers = new HashSet<>();

        for (Policy policy : completedPolicies) {
            totalPersonsInsured += policy.getPersonCount();
            uniqueCustomers.add(policy.getCreatedByUser().getUserId());
            totalPaxSold += calculatePaxForPolicy(policy);
        }

        BigDecimal unassignedPaymentsAmount = startDate != null || endDateUtc != null
                ? paymentRepository.sumCompletedPaymentsWithoutPolicyByDateRange(startDate, endDateUtc)
                : paymentRepository.sumAllCompletedPaymentsWithoutPolicy();

        long approximatePax = unassignedPaymentsAmount != null && unassignedPaymentsAmount.compareTo(BigDecimal.ZERO) > 0
                ? unassignedPaymentsAmount.divide(BigDecimal.valueOf(3000), 0, RoundingMode.DOWN).longValue()
                : 0;

        long totalPaxWithApproximate = totalPaxSold + approximatePax;

        log.info("📊 PAX Reales: {}, PAX Aproximados: {} (${} ÷ 3000), Total PAX: {}",
                totalPaxSold, approximatePax, unassignedPaymentsAmount, totalPaxWithApproximate);


        // Calcular promedios
        BigDecimal averageOrderValue = completedPolicies.isEmpty()
                ? BigDecimal.ZERO
                : totalRevenue.divide(
                BigDecimal.valueOf(completedPolicies.size()),
                2,
                RoundingMode.HALF_UP
        );

        double averagePersonsPerPolicy = completedPolicies.isEmpty()
                ? 0.0
                : (double) totalPersonsInsured / completedPolicies.size();

        // ✅ Contar por estado usando método optimizado
        long completedCount = paymentRepository.countByStatus(PaymentStatus.COMPLETED);
        long pendingCount = paymentRepository.countByStatus(PaymentStatus.PENDING);
        long failedCount = paymentRepository.countByStatus(PaymentStatus.FAILED);

        log.info("📊 Estados - COMPLETED: {}, PENDING: {}, FAILED: {}",
                completedCount, pendingCount, failedCount);

        // Sales por tipo de póliza
        List<AdminSalesKPIResponse.PolicyTypeSalesDTO> salesByType =
                calculateSalesByPolicyType(completedPolicies);

        // Sales por método de pago
        List<AdminSalesKPIResponse.PaymentMethodSalesDTO> salesByPaymentMethod =
                calculateSalesByPaymentMethod(completedPolicies);

        // Revenue por periodo
        AdminSalesKPIResponse.RevenueByPeriodDTO revenueByPeriod =
                calculateRevenueByPeriod();

        // Estadísticas de descuentos
        AdminSalesKPIResponse.DiscountStatsDTO discountStats =
                calculateDiscountStats(completedPolicies);

        AdminSalesKPIResponse kpis = new AdminSalesKPIResponse(
                totalRevenue,
                (long) completedPolicies.size(),
                totalPersonsInsured,
                (long) uniqueCustomers.size(),
                totalPaxSold,
                totalPaxWithApproximate,
                completedCount,
                pendingCount,
                failedCount,
                averageOrderValue,
                averagePersonsPerPolicy,
                salesByType,
                salesByPaymentMethod,
                revenueByPeriod,
                discountStats
        );

        log.info("✅ KPIs calculados - Revenue: ${}, Pólizas: {}, PAX: {}, Payments: {}",
                totalRevenue, completedPolicies.size(), totalPaxSold, completedPaymentsCount);

        return ApiResponse.success("KPIs obtenidos exitosamente", kpis);
    }

    /**
     * Calcula los PAX (Passenger Days/Nights) para una póliza
     * - Turista: días × personas
     * - Hotelero: noches × personas
     */
    private long calculatePaxForPolicy(Policy policy) {
        try {
            PolicyDetail detail = policyDetailRepository.findByPolicyId(policy).orElse(null);

            if (detail == null || detail.getDeparture() == null || detail.getArrival() == null) {
                log.warn("⚠️ Póliza {} sin fechas completas", policy.getPolicyId());
                return 0;
            }

            int persons = policy.getPersonCount();

            ZonedDateTime departure = detail.getDeparture();
            ZonedDateTime arrival = detail.getArrival();

            long pax;

            if (policy.getPolicyType().getPolicyTypeId().equals(2L) || policy.getPolicyType().getPolicyTypeId().equals(3L)) {
                // Hotelero: noches × personas
                long nights = ChronoUnit.DAYS.between(departure.toLocalDate(), arrival.toLocalDate());
                if (nights < 0) nights = 0;
                pax = nights * persons;
                log.debug("🏨 Póliza {}: {} noches × {} personas = {} PAX",
                        policy.getPolicyId(), nights, persons, pax);
            } else{
                // Turista: días × personas
                long days = ChronoUnit.DAYS.between(departure.toLocalDate(), arrival.toLocalDate()) + 1;
                if (days < 1) days = 1;
                pax = days * persons;
                log.debug("✈️ Póliza {}: {} días × {} personas = {} PAX",
                        policy.getPolicyId(), days, persons, pax);
            }

            return pax;

        } catch (Exception e) {
            log.error("❌ Error calculando PAX para póliza {}: {}",
                    policy.getPolicyId(), e.getMessage());
            return 0;
        }
    }

    // ============ RESTO DE MÉTODOS SIN CAMBIOS ============

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<AdminPolicyDetailResponse>> getAllPoliciesWithDetails(
            int page, int size, String status, ZonedDateTime startDate, ZonedDateTime endDate) {

        log.info("📊 Admin: obteniendo pólizas - page: {}, size: {}, status: {}", page, size, status);

        PageRequest pageable = PageRequest.of(page, size);
        Page<Policy> policyPage;

        if (status != null && !status.isEmpty()) {
            PaymentStatus paymentStatus = PaymentStatus.valueOf(status);
            policyPage = policyRepository.findAllByPaymentStatus(paymentStatus, pageable);
        } else if (startDate != null || endDate != null) {
            policyPage = policyRepository.findAllByDateRange(startDate, endDate, pageable);
        } else {
            policyPage = policyRepository.findAllOrderByCreatedAtDesc(pageable);
        }

        List<AdminPolicyDetailResponse> responses = policyPage.getContent().stream()
                .map(this::mapToAdminPolicyDetail)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Pagination pagination = new Pagination(
                policyPage.getNumber(),
                policyPage.getSize(),
                policyPage.getTotalElements(),
                policyPage.getTotalPages()
        );

        log.info("✅ {} pólizas obtenidas", responses.size());

        return ApiResponse.success(
                String.format("Se obtuvieron %d pólizas", responses.size()),
                responses,
                pagination
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<AdminPolicyDetailResponse> getPolicyDetailById(Long policyId) {
        log.info("🔍 Admin: obteniendo detalle de póliza {}", policyId);

        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Póliza no encontrada: " + policyId));

        AdminPolicyDetailResponse response = mapToAdminPolicyDetail(policy);

        return ApiResponse.success("Detalle de póliza obtenido", response);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePoliciesConsolidatedExcel(
            ZonedDateTime startDate, ZonedDateTime endDate) {

        log.info("📊 Admin: generando Excel consolidado");

        List<Policy> policies = startDate != null || endDate != null
                ? policyRepository.findCompletedPoliciesByDateRange(startDate, endDate)
                : policyRepository.findAllCompletedPolicies();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Consolidado Pólizas");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle moneyStyle = createMoneyStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "ID", "Número Póliza", "Cliente", "Email", "Teléfono",
                    "Tipo Póliza", "Personas", "PAX", "Monto Total", "Descuento",
                    "Origen", "Destino", "Salida", "Llegada",
                    "Estado Pago", "Fecha Creación", "Fecha Pago"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            BigDecimal grandTotal = BigDecimal.ZERO;

            for (Policy policy : policies) {
                Row row = sheet.createRow(rowNum++);

                PolicyDetail detail = policyDetailRepository.findByPolicyId(policy).orElse(null);
                PolicyPayment policyPayment = policyPaymentRepository.findByPolicy(policy).orElse(null);
                Payment payment = policyPayment != null ? policyPayment.getPayment() : null;
                User user = policy.getCreatedByUser();

                int col = 0;
                createCell(row, col++, policy.getPolicyId().toString(), dataStyle);
                createCell(row, col++, policy.getPolicyNumber(), dataStyle);
                createCell(row, col++, user.getPerson().getFullName(), dataStyle);
                createCell(row, col++, user.getEmail(), dataStyle);
                createCell(row, col++, user.getPhone(), dataStyle);
                createCell(row, col++, policy.getPolicyType().getName(), dataStyle);
                createCell(row, col++, policy.getPersonCount().toString(), dataStyle);

                // PAX
                long pax = calculatePaxForPolicy(policy);
                createCell(row, col++, String.valueOf(pax), dataStyle);

                if (policyPayment != null) {
                    Cell amountCell = row.createCell(col++);
                    amountCell.setCellValue(policyPayment.getAppliedAmount().doubleValue());
                    amountCell.setCellStyle(moneyStyle);
                    grandTotal = grandTotal.add(policyPayment.getAppliedAmount());
                } else {
                    createCell(row, col++, "N/A", dataStyle);
                }

                createCell(row, col++,
                        policy.getDiscount() != null ? policy.getDiscount().getName() : "Sin descuento",
                        dataStyle);

                createCell(row, col++, detail != null ? detail.getOrigin() : "N/A", dataStyle);
                createCell(row, col++, detail != null ? detail.getDestination() : "N/A", dataStyle);

                if (detail != null && detail.getDeparture() != null) {
                    Cell dateCell = row.createCell(col++);
                    dateCell.setCellValue(Date.from(detail.getDeparture().toInstant()));
                    dateCell.setCellStyle(dateStyle);
                } else {
                    createCell(row, col++, "N/A", dataStyle);
                }

                if (detail != null && detail.getArrival() != null) {
                    Cell dateCell = row.createCell(col++);
                    dateCell.setCellValue(Date.from(detail.getArrival().toInstant()));
                    dateCell.setCellStyle(dateStyle);
                } else {
                    createCell(row, col++, "N/A", dataStyle);
                }

                createCell(row, col++,
                        payment != null ? payment.getStatus().name() : "N/A",
                        dataStyle);

                Cell createdCell = row.createCell(col++);
                createdCell.setCellValue(Date.from(policy.getCreatedAt().toInstant()));
                createdCell.setCellStyle(dateStyle);

                if (payment != null && payment.getUpdatedAt() != null) {
                    Cell paidCell = row.createCell(col++);
                    paidCell.setCellValue(Date.from(payment.getUpdatedAt().toInstant()));
                    paidCell.setCellStyle(dateStyle);
                } else {
                    createCell(row, col++, "N/A", dataStyle);
                }
            }

            Row totalRow = sheet.createRow(rowNum);
            Cell totalLabelCell = totalRow.createCell(6);
            totalLabelCell.setCellValue("TOTAL GENERAL:");
            totalLabelCell.setCellStyle(headerStyle);

            Cell totalAmountCell = totalRow.createCell(8);
            totalAmountCell.setCellValue(grandTotal.doubleValue());
            totalAmountCell.setCellStyle(moneyStyle);

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 500);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            log.info("✅ Excel generado - {} pólizas, Total: {}", policies.size(), grandTotal);

            return outputStream.toByteArray();

        } catch (IOException ex) {
            log.error("❌ Error generando Excel: {}", ex.getMessage(), ex);
            throw new RuntimeException("Error generando Excel", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadPolicyFilesAsZip(Long policyId) {
        log.info("📦 Admin: descargando archivos de póliza {}", policyId);

        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Póliza no encontrada: " + policyId));

        List<PolicyFile> files = policyFileRepository.findByPolicyId(policyId);

        if (files.isEmpty()) {
            throw new IllegalArgumentException("No hay archivos para esta póliza");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (PolicyFile file : files) {
                byte[] fileData = fileAppService.downloadFile(file.getFileId());
                File dataFile = fileRepository.findById(file.getFileId()).orElseThrow();

                ZipEntry entry = new ZipEntry(dataFile.getOriginalName());
                zos.putNextEntry(entry);
                zos.write(fileData);
                zos.closeEntry();
            }

            zos.finish();

            log.info("✅ ZIP generado con {} archivos", files.size());

            return baos.toByteArray();

        } catch (IOException ex) {
            log.error("❌ Error generando ZIP: {}", ex.getMessage(), ex);
            throw new RuntimeException("Error al generar ZIP", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<PolicyTypeAnalyticsResponse>> getPolicyTypeAnalytics(
            ZonedDateTime startDate, ZonedDateTime endDate) {
        log.info("📊 Admin: calculando analytics por tipo de póliza");
        return ApiResponse.success("Analytics obtenidos", List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<UserAnalyticsResponse> getUserAnalytics() {
        log.info("👥 Admin: calculando analytics de usuarios");
        return ApiResponse.success("User analytics obtenidos", new UserAnalyticsResponse());
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<AdminPolicyDetailResponse>> getPendingPaymentPolicies(int page, int size) {
        log.info("⏳ Admin: obteniendo pólizas pendientes de pago");

        PageRequest pageable = PageRequest.of(page, size);
        Page<Policy> policyPage = policyRepository.findAllByPaymentStatus(PaymentStatus.PENDING, pageable);

        List<AdminPolicyDetailResponse> responses = policyPage.getContent().stream()
                .map(this::mapToAdminPolicyDetail)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Pagination pagination = new Pagination(
                policyPage.getNumber(),
                policyPage.getSize(),
                policyPage.getTotalElements(),
                policyPage.getTotalPages()
        );

        return ApiResponse.success(
                String.format("%d pólizas pendientes", responses.size()),
                responses,
                pagination
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<AdminPolicyDetailResponse>> searchPolicies(
            String query, Long userId, Long policyTypeId, int page, int size) {

        log.info("🔎 Admin: buscando pólizas - query: {}, userId: {}, policyTypeId: {}",
                query, userId, policyTypeId);

        PageRequest pageable = PageRequest.of(page, size);
        Page<Policy> policyPage = policyRepository.searchPolicies(query, userId, policyTypeId, pageable);

        List<AdminPolicyDetailResponse> responses = policyPage.getContent().stream()
                .map(this::mapToAdminPolicyDetail)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Pagination pagination = new Pagination(
                policyPage.getNumber(),
                policyPage.getSize(),
                policyPage.getTotalElements(),
                policyPage.getTotalPages()
        );

        return ApiResponse.success(
                String.format("%d resultados encontrados", responses.size()),
                responses,
                pagination
        );
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateSalesReportExcel(ZonedDateTime startDate, ZonedDateTime endDate) {
        log.info("📊 Generando reporte de ventas general - desde: {} hasta: {}", startDate, endDate);

        // Convertir de America/Bogota a UTC antes de consultar
        ZonedDateTime startDateUtc = startDate != null
                ? startDate.withZoneSameInstant(java.time.ZoneId.of("UTC"))
                : null;
        ZonedDateTime endDateUtc = endDate != null
                ? endDate
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(999_999_999)
                .withZoneSameInstant(ZoneId.of("UTC"))
                : null;

        log.info("🕐 Conversión zona horaria - Bogotá: [{} -> {}] | UTC: [{} -> {}]",
                startDate, endDate, startDateUtc, endDateUtc);

        List<Payment> payments = paymentRepository.findCompletedPaymentsBetweenDates(startDateUtc, endDateUtc);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Reporte de Ventas");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle moneyStyle = createMoneyStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "Transaction ID", "Monto", "Email Usuario",
                    "Teléfono", "Tipo Documento", "Número Documento",
                    "Fecha Actualización"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (Payment payment : payments) {
                Row row = sheet.createRow(rowNum++);
                User user = payment.getUser();
                Person person = user.getPerson();

                int col = 0;
                createCell(row, col++, payment.getTransactionId(), dataStyle);

                Cell amountCell = row.createCell(col++);
                amountCell.setCellValue(payment.getAmount().doubleValue());
                amountCell.setCellStyle(moneyStyle);
                totalAmount = totalAmount.add(payment.getAmount());

                createCell(row, col++, user.getEmail(), dataStyle);
                createCell(row, col++, user.getPhone() != null ? user.getPhone() : "N/A", dataStyle);
                createCell(row, col++, person != null && person.getDocumentType() != null
                        ? person.getDocumentType().toString() : "N/A", dataStyle);
                createCell(row, col++, person != null && person.getDocumentNumber() != null
                        ? person.getDocumentNumber() : "N/A", dataStyle);

                Cell updatedCell = row.createCell(col++);
                updatedCell.setCellValue(Date.from(payment.getUpdatedAt().toInstant()));
                updatedCell.setCellStyle(dateStyle);
            }

            Row totalRow = sheet.createRow(rowNum);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("TOTAL:");
            totalLabelCell.setCellStyle(headerStyle);

            Cell totalAmountCell = totalRow.createCell(1);
            totalAmountCell.setCellValue(totalAmount.doubleValue());
            totalAmountCell.setCellStyle(moneyStyle);

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 500);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            log.info("✅ Reporte generado - {} pagos, Total: ${}", payments.size(), totalAmount);

            return outputStream.toByteArray();

        } catch (IOException ex) {
            log.error("❌ Error generando reporte de ventas: {}", ex.getMessage(), ex);
            throw new RuntimeException("Error generando reporte de ventas", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateAdvisorSalesReportExcel(String advisorCode, ZonedDateTime startDate, ZonedDateTime endDate) {
        log.info("📊 Generando reporte de ventas para asesor: {} - desde: {} hasta: {}",
                advisorCode, startDate, endDate);

        // Convertir de America/Bogota a UTC antes de consultar
        ZonedDateTime startDateUtc = startDate != null
                ? startDate.withZoneSameInstant(java.time.ZoneId.of("UTC"))
                : null;
        ZonedDateTime endDateUtc = endDate != null
                ? endDate
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(999_999_999)
                .withZoneSameInstant(ZoneId.of("UTC"))
                : null;

        log.info("🕐 Conversión zona horaria - Bogotá: [{} -> {}] | UTC: [{} -> {}]",
                startDate, endDate, startDateUtc, endDateUtc);

        // 1. Obtener usuarios que usaron este código
        List<Long> userIds = paymentRepository.findUserIdsByAdvisorCode(advisorCode);

        if (userIds.isEmpty()) {
            log.warn("⚠️ No se encontraron usuarios que hayan usado el código: {}", advisorCode);
            throw new IllegalArgumentException("No hay ventas para el código de asesor: " + advisorCode);
        }

        log.info("✅ Encontrados {} usuarios que usaron el código {}", userIds.size(), advisorCode);

        // 2. Obtener pagos de esos usuarios en el rango de fechas (ya en UTC)
        List<Payment> payments = paymentRepository.findCompletedPaymentsByUserIdsAndDateRange(
                userIds, startDateUtc, endDateUtc);

        if (payments.isEmpty()) {
            log.warn("⚠️ No hay pagos en el rango de fechas para el código: {}", advisorCode);
            throw new IllegalArgumentException(
                    String.format("No hay ventas en el período especificado para el asesor: %s", advisorCode));
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Ventas Asesor " + advisorCode);

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle moneyStyle = createMoneyStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "Transaction ID", "Monto", "Email Usuario",
                    "Teléfono", "Tipo Documento", "Número Documento",
                    "Fecha Actualización"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (Payment payment : payments) {
                Row row = sheet.createRow(rowNum++);
                User user = payment.getUser();
                Person person = user.getPerson();

                int col = 0;
                createCell(row, col++, payment.getTransactionId(), dataStyle);

                Cell amountCell = row.createCell(col++);
                amountCell.setCellValue(payment.getAmount().doubleValue());
                amountCell.setCellStyle(moneyStyle);
                totalAmount = totalAmount.add(payment.getAmount());

                createCell(row, col++, user.getEmail(), dataStyle);
                createCell(row, col++, user.getPhone() != null ? user.getPhone() : "N/A", dataStyle);
                createCell(row, col++, person != null && person.getDocumentType() != null
                        ? person.getDocumentType().toString() : "N/A", dataStyle);
                createCell(row, col++, person != null && person.getDocumentNumber() != null
                        ? person.getDocumentNumber() : "N/A", dataStyle);

                Cell updatedCell = row.createCell(col++);
                updatedCell.setCellValue(Date.from(payment.getUpdatedAt().toInstant()));
                updatedCell.setCellStyle(dateStyle);
            }

            Row totalRow = sheet.createRow(rowNum);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("TOTAL:");
            totalLabelCell.setCellStyle(headerStyle);

            Cell totalAmountCell = totalRow.createCell(1);
            totalAmountCell.setCellValue(totalAmount.doubleValue());
            totalAmountCell.setCellStyle(moneyStyle);

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 500);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            log.info("✅ Reporte de asesor generado - {} pagos, Total: ${}", payments.size(), totalAmount);

            return outputStream.toByteArray();

        } catch (IOException ex) {
            log.error("❌ Error generando reporte de asesor: {}", ex.getMessage(), ex);
            throw new RuntimeException("Error generando reporte de asesor", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<AdvisorCodeResponse>> getAllAdvisorCodes() {
        log.info("📋 Obteniendo todos los códigos ADVISOR");

        List<Discount> advisorDiscounts = discountRepository.findByType(DiscountType.ADVISOR);

        List<AdvisorCodeResponse> responses = advisorDiscounts.stream()
                .map(d -> AdvisorCodeResponse.builder()
                        .discountId(d.getDiscountId())
                        .codigo(d.getName())
                        .tipo(d.getType().name())
                        .activo(d.getActive())
                        .build())
                .collect(Collectors.toList());

        log.info("✅ Encontrados {} códigos ADVISOR", responses.size());

        return ApiResponse.success(
                String.format("Se encontraron %d códigos de asesor", responses.size()),
                responses
        );
    }


    // ============ MÉTODOS AUXILIARES ============

    private AdminPolicyDetailResponse mapToAdminPolicyDetail(Policy policy) {
        try {
            PolicyDetail detail = policyDetailRepository.findByPolicyId(policy).orElse(null);
            PolicyPayment policyPayment = policyPaymentRepository.findByPolicy(policy).orElse(null);
            Payment payment = policyPayment != null ? policyPayment.getPayment() : null;
            List<PolicyPerson> policyPersons = policyPersonRepository.findByPolicyId(policy.getPolicyId());
            List<PolicyFile> policyFiles = policyFileRepository.findByPolicyId(policy.getPolicyId());
            List<File> files = new ArrayList<>();

            for (PolicyFile policyFile : policyFiles) {
                File dataFile = fileRepository.findById(policyFile.getFileId()).orElse(null);
                if (dataFile != null) {
                    files.add(dataFile);
                }
            }

            AdminPolicyDetailResponse response = new AdminPolicyDetailResponse();

            response.setPolicyId(policy.getPolicyId());
            response.setPolicyNumber(policy.getPolicyNumber());
            response.setPersonCount(policy.getPersonCount());
            response.setPolicyTypeName(policy.getPolicyType().getName());
            response.setPolicyTypeId(policy.getPolicyType().getPolicyTypeId());
            response.setUnitPriceWithDiscount(policy.getUnitPriceWithDiscount());
            response.setDiscountCode(policy.getDiscount() != null ? policy.getDiscount().getName() : null);
            response.setCreatedWithFile(policy.getCreatedWithFile());
            response.setCreatedAt(policy.getCreatedAt());

            if (detail != null) {
                response.setOrigin(detail.getOrigin());
                response.setDestination(detail.getDestination());
                response.setDeparture(detail.getDeparture());
                response.setArrival(detail.getArrival());
            }

            if (payment != null) {
                response.setPayment(new AdminPolicyDetailResponse.PaymentInfoDTO(
                        payment.getPaymentId(),
                        payment.getTransactionId(),
                        payment.getAmount(),
                        payment.getStatus(),
                        payment.getPaymentType().getName(),
                        payment.getUpdatedAt()
                ));
            }

            User user = policy.getCreatedByUser();
            Person person = user.getPerson();
            response.setUser(new AdminPolicyDetailResponse.UserInfoDTO(
                    user.getUserId(),
                    user.getEmail(),
                    person.getFullName(),
                    user.getPhone(),
                    person.getDocumentType(),
                    person.getDocumentNumber()
            ));

            List<AdminPolicyDetailResponse.InsuredPersonDTO> insuredList = policyPersons.stream()
                    .map(pp -> new AdminPolicyDetailResponse.InsuredPersonDTO(
                            pp.getPerson().getPersonId(),
                            pp.getPerson().getFullName(),
                            pp.getPerson().getDocumentType(),
                            pp.getPerson().getDocumentNumber(),
                            pp.getRelationship()
                    ))
                    .collect(Collectors.toList());
            response.setInsuredPersons(insuredList);

            List<AdminPolicyDetailResponse.FileInfoDTO> fileList = files.stream()
                    .map(f -> new AdminPolicyDetailResponse.FileInfoDTO(
                            f.getFileId(),
                            f.getOriginalName(),
                            f.getContentType(),
                            f.getSize(),
                            f.getFileUrl(),
                            f.getCreatedAt()
                    ))
                    .collect(Collectors.toList());
            response.setFiles(fileList);

            return response;

        } catch (Exception ex) {
            log.error("❌ Error mapeando póliza {}: {}", policy.getPolicyId(), ex.getMessage());
            return null;
        }
    }

    private List<AdminSalesKPIResponse.PolicyTypeSalesDTO> calculateSalesByPolicyType(
            List<Policy> policies) {

        Map<String, List<Policy>> groupedByType = policies.stream()
                .collect(Collectors.groupingBy(p -> p.getPolicyType().getName()));

        return groupedByType.entrySet().stream()
                .map(entry -> {
                    String typeName = entry.getKey();
                    List<Policy> typePolicies = entry.getValue();

                    BigDecimal revenue = typePolicies.stream()
                            .map(p -> {
                                PolicyPayment pp = policyPaymentRepository.findByPolicy(p).orElse(null);
                                return pp != null ? pp.getAppliedAmount() : BigDecimal.ZERO;
                            })
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    double percentage = ((double) typePolicies.size() / policies.size()) * 100;

                    return new AdminSalesKPIResponse.PolicyTypeSalesDTO(
                            typeName,
                            (long) typePolicies.size(),
                            revenue,
                            percentage
                    );
                })
                .sorted((a, b) -> b.getRevenue().compareTo(a.getRevenue()))
                .collect(Collectors.toList());
    }

    private List<AdminSalesKPIResponse.PaymentMethodSalesDTO> calculateSalesByPaymentMethod(
            List<Policy> policies) {

        Map<String, List<Policy>> groupedByPaymentMethod = new HashMap<>();

        for (Policy policy : policies) {
            PolicyPayment pp = policyPaymentRepository.findByPolicy(policy).orElse(null);
            if (pp != null) {
                String method = pp.getPayment().getPaymentType().getName();
                groupedByPaymentMethod.computeIfAbsent(method, k -> new ArrayList<>()).add(policy);
            }
        }

        return groupedByPaymentMethod.entrySet().stream()
                .map(entry -> {
                    String method = entry.getKey();
                    List<Policy> methodPolicies = entry.getValue();

                    BigDecimal revenue = methodPolicies.stream()
                            .map(p -> {
                                PolicyPayment pp = policyPaymentRepository.findByPolicy(p).orElse(null);
                                return pp != null ? pp.getAppliedAmount() : BigDecimal.ZERO;
                            })
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new AdminSalesKPIResponse.PaymentMethodSalesDTO(
                            method,
                            (long) methodPolicies.size(),
                            revenue
                    );
                })
                .collect(Collectors.toList());
    }

    private AdminSalesKPIResponse.RevenueByPeriodDTO calculateRevenueByPeriod() {
        ZonedDateTime now = ZonedDateTime.now();

        BigDecimal last7Days = paymentRepository.sumCompletedPaymentsAmountByDateRange(
                now.minusDays(7), now);
        BigDecimal last30Days = paymentRepository.sumCompletedPaymentsAmountByDateRange(
                now.minusDays(30), now);
        BigDecimal last90Days = paymentRepository.sumCompletedPaymentsAmountByDateRange(
                now.minusDays(90), now);
        BigDecimal allTime = paymentRepository.sumAllCompletedPaymentsAmount();

        return new AdminSalesKPIResponse.RevenueByPeriodDTO(
                last7Days, last30Days, last90Days, allTime
        );
    }

    private BigDecimal calculateRevenueForPeriod(ZonedDateTime start, ZonedDateTime end) {
        List<Policy> policies = policyRepository.findCompletedPoliciesByDateRange(start, end);

        return policies.stream()
                .map(p -> {
                    PolicyPayment pp = policyPaymentRepository.findByPolicy(p).orElse(null);
                    return pp != null ? pp.getAppliedAmount() : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private AdminSalesKPIResponse.DiscountStatsDTO calculateDiscountStats(List<Policy> policies) {
        List<Policy> policiesWithDiscount = policies.stream()
                .filter(p -> p.getDiscount() != null)
                .collect(Collectors.toList());

        BigDecimal totalDiscounted = BigDecimal.ZERO;

        Map<String, Long> discountUsage = policiesWithDiscount.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getDiscount().getName(),
                        Collectors.counting()
                ));

        List<AdminSalesKPIResponse.DiscountUsageDTO> topDiscounts = discountUsage.entrySet().stream()
                .map(e -> new AdminSalesKPIResponse.DiscountUsageDTO(
                        e.getKey(),
                        e.getValue(),
                        BigDecimal.ZERO
                ))
                .sorted((a, b) -> b.getUsageCount().compareTo(a.getUsageCount()))
                .limit(10)
                .collect(Collectors.toList());

        return new AdminSalesKPIResponse.DiscountStatsDTO(
                (long) policiesWithDiscount.size(),
                totalDiscounted,
                topDiscounts
        );
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setDataFormat(workbook.createDataFormat().getFormat("dd/MM/yyyy HH:mm"));
        return style;
    }

    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private CellStyle createMoneyStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("$#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }
}