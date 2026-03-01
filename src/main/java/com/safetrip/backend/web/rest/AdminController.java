package com.safetrip.backend.web.rest;

import com.safetrip.backend.application.service.AdminService;
import com.safetrip.backend.web.dto.response.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    /**
     * Obtiene todas las pólizas con información completa
     * GET /api/admin/policies?page=0&size=20&status=COMPLETED
     */
    @GetMapping("/policies")
    public ResponseEntity<ApiResponse<List<AdminPolicyDetailResponse>>> getAllPolicies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate) {

        log.info("🔍 Admin solicitando pólizas - page: {}, size: {}, status: {}", page, size, status);

        ApiResponse<List<AdminPolicyDetailResponse>> response = adminService.getAllPoliciesWithDetails(
                page, size, status, startDate, endDate
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene una póliza específica con todos sus detalles
     * GET /api/admin/policies/123
     */
    @GetMapping("/policies/{policyId}")
    public ResponseEntity<ApiResponse<AdminPolicyDetailResponse>> getPolicyById(
            @PathVariable Long policyId) {

        log.info("🔍 Admin solicitando póliza: {}", policyId);

        ApiResponse<AdminPolicyDetailResponse> response = adminService.getPolicyDetailById(policyId);

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene KPIs de ventas y estadísticas generales
     * GET /api/admin/kpis/sales?startDate=...&endDate=...
     */
    @GetMapping("/kpis/sales")
    public ResponseEntity<ApiResponse<AdminSalesKPIResponse>> getSalesKPIs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate) {

        log.info("📊 Admin solicitando KPIs de ventas");

        ApiResponse<AdminSalesKPIResponse> response = adminService.getSalesKPIs(startDate, endDate);

        return ResponseEntity.ok(response);
    }

    /**
     * Descarga reporte consolidado de todas las pólizas en Excel
     * GET /api/admin/reports/policies/excel
     */
    @GetMapping("/reports/policies/excel")
    public ResponseEntity<byte[]> downloadAllPoliciesExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate) {

        log.info("📥 Admin descargando reporte Excel de pólizas");

        byte[] excelData = adminService.generatePoliciesConsolidatedExcel(startDate, endDate);

        String filename = String.format("polizas_consolidado_%s.xlsx",
                ZonedDateTime.now().toLocalDate());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }

    /**
     * Descarga archivos adjuntos de una póliza específica como ZIP
     * GET /api/admin/policies/123/files/download
     */
    @GetMapping("/policies/{policyId}/files/download")
    public ResponseEntity<byte[]> downloadPolicyFiles(@PathVariable Long policyId) {

        log.info("📎 Admin descargando archivos de póliza: {}", policyId);

        byte[] zipData = adminService.downloadPolicyFilesAsZip(policyId);

        String filename = String.format("poliza_%d_archivos.zip", policyId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zipData);
    }

    /**
     * Obtiene estadísticas por tipo de póliza
     * GET /api/admin/analytics/policy-types
     */
    @GetMapping("/analytics/policy-types")
    public ResponseEntity<ApiResponse<?>> getPolicyTypeAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate) {

        log.info("📈 Admin solicitando analytics por tipo de póliza");

        ApiResponse<?> response = adminService.getPolicyTypeAnalytics(startDate, endDate);

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene estadísticas de usuarios
     * GET /api/admin/analytics/users
     */
    @GetMapping("/analytics/users")
    public ResponseEntity<ApiResponse<?>> getUserAnalytics() {

        log.info("👥 Admin solicitando analytics de usuarios");

        ApiResponse<?> response = adminService.getUserAnalytics();

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene pólizas pendientes de pago
     * GET /api/admin/policies/pending-payment
     */
    @GetMapping("/policies/pending-payment")
    public ResponseEntity<ApiResponse<List<AdminPolicyDetailResponse>>> getPendingPaymentPolicies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("⏳ Admin solicitando pólizas pendientes de pago");

        ApiResponse<List<AdminPolicyDetailResponse>> response = adminService.getPendingPaymentPolicies(page, size);

        return ResponseEntity.ok(response);
    }

    /**
     * Buscar pólizas por criterios múltiples
     * GET /api/admin/policies/search?query=...&userId=...
     */
    @GetMapping("/policies/search")
    public ResponseEntity<ApiResponse<List<AdminPolicyDetailResponse>>> searchPolicies(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long policyTypeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("🔎 Admin buscando pólizas - query: {}, userId: {}, policyTypeId: {}",
                query, userId, policyTypeId);

        ApiResponse<List<AdminPolicyDetailResponse>> response = adminService.searchPolicies(
                query, userId, policyTypeId, page, size
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/reports/sales/excel")
    public ResponseEntity<byte[]> downloadSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate) {

        log.info("📥 Admin descargando reporte de ventas general");

        byte[] excelData = adminService.generateSalesReportExcel(startDate, endDate);

        String filename = String.format("ventas_desde_%s_hasta_%s.xlsx",
                startDate.toLocalDate(),
                endDate.toLocalDate());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }

    /**
     * Descarga reporte de ventas por asesor en Excel
     * GET /api/admin/reports/sales/advisor/excel?advisorCode=JUAN&startDate=...&endDate=...
     */
    @GetMapping("/reports/sales/advisor/excel")
    public ResponseEntity<byte[]> downloadAdvisorSalesReport(
            @RequestParam String advisorCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate) {

        log.info("📥 Admin descargando reporte de ventas para asesor: {}", advisorCode);

        byte[] excelData = adminService.generateAdvisorSalesReportExcel(advisorCode, startDate, endDate);

        String filename = String.format("ventas_desde_%s_hasta_%s_asesor_%s.xlsx",
                startDate.toLocalDate(),
                endDate.toLocalDate(),
                advisorCode);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }

    /**
     * Obtiene lista de todos los códigos ADVISOR
     * GET /api/admin/discounts/advisor
     */
    @GetMapping("/discounts/advisor")
    public ResponseEntity<ApiResponse<List<AdvisorCodeResponse>>> getAllAdvisorCodes() {

        log.info("📋 Admin solicitando códigos ADVISOR");

        ApiResponse<List<AdvisorCodeResponse>> response = adminService.getAllAdvisorCodes();

        return ResponseEntity.ok(response);
    }
}