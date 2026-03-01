package com.safetrip.backend.application.service;

import com.safetrip.backend.web.dto.response.*;

import java.time.ZonedDateTime;
import java.util.List;

public interface AdminService {

    ApiResponse<List<AdminPolicyDetailResponse>> getAllPoliciesWithDetails(
            int page, int size, String status, ZonedDateTime startDate, ZonedDateTime endDate);
    ApiResponse<AdminPolicyDetailResponse> getPolicyDetailById(Long policyId);
    ApiResponse<AdminSalesKPIResponse> getSalesKPIs(ZonedDateTime startDate, ZonedDateTime endDate);
    byte[] generatePoliciesConsolidatedExcel(ZonedDateTime startDate, ZonedDateTime endDate);
    byte[] downloadPolicyFilesAsZip(Long policyId);
    ApiResponse<List<PolicyTypeAnalyticsResponse>> getPolicyTypeAnalytics(
            ZonedDateTime startDate, ZonedDateTime endDate);
    ApiResponse<UserAnalyticsResponse> getUserAnalytics();
    ApiResponse<List<AdminPolicyDetailResponse>> getPendingPaymentPolicies(int page, int size);
    ApiResponse<List<AdminPolicyDetailResponse>> searchPolicies(
            String query, Long userId, Long policyTypeId, int page, int size);
    byte[] generateSalesReportExcel(ZonedDateTime startDate, ZonedDateTime endDate);
    byte[] generateAdvisorSalesReportExcel(String advisorCode, ZonedDateTime startDate, ZonedDateTime endDate);
    ApiResponse<List<AdvisorCodeResponse>> getAllAdvisorCodes();
}
