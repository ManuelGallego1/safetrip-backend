package com.safetrip.backend.application.service;

import com.safetrip.backend.web.dto.request.CreatePolicyRequest;
import com.safetrip.backend.web.dto.response.ApiResponse;
import com.safetrip.backend.web.dto.response.CreatePolicyResponse;
import com.safetrip.backend.web.dto.response.PolicyResponseWithDetails;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public interface PolicyService {

    /**
     * Crea una póliza preliminar detectando automáticamente el tipo de fuente de datos
     */
    CreatePolicyResponse createPreliminaryPolicy(
            CreatePolicyRequest request,
            MultipartFile dataFile,
            MultipartFile[] attachments) throws IOException;

    /**
     * Obtiene todas las pólizas del usuario autenticado
     */
    ApiResponse<List<PolicyResponseWithDetails>> getAllPoliciesForUser(int page, int size);

    /**
     * Actualiza información básica de una póliza
     */
    int patchPolicy(Long policyId, String policyNumber, BigDecimal unitPrice, Integer personCount);
}