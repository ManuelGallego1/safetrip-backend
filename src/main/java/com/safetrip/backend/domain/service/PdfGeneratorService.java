package com.safetrip.backend.domain.service;

import com.safetrip.backend.infrastructure.integration.pdf.dto.PolicyPdfData;

import java.io.OutputStream;

public interface PdfGeneratorService {
    byte[] generatePolicyPdf(PolicyPdfData data);
    void generatePolicyPdf(PolicyPdfData data, OutputStream outputStream);
}