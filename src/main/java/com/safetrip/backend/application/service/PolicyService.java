package com.safetrip.backend.application.service;

import com.safetrip.backend.web.dto.request.CreatePolicyRequest;
import com.safetrip.backend.web.dto.response.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public interface PolicyService {

    CreatePolicyResponse createPreliminaryPolicy(
            CreatePolicyRequest request,
            MultipartFile dataFile,
            MultipartFile[] attachments) throws IOException;
    ApiResponse<List<PolicyResponseWithDetails>> getAllPoliciesForUser(int page, int size);
    int patchPolicy(Long policyId, String policyNumber, BigDecimal unitPrice, Integer personCount);
    byte[] downloadPoliciesConsolidatedExcel();
    ApiResponse<List<InsuredPersonResponse>> getInsuredPersonsByPolicy(Long policyId);
    PolicyTypePriceResponse getPriceByPolicyTypeId(Long policyTypeId);
}