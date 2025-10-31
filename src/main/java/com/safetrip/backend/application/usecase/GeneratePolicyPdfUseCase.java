package com.safetrip.backend.application.usecase;

import com.safetrip.backend.domain.service.PdfGeneratorService;
import com.safetrip.backend.infrastructure.integration.pdf.dto.PolicyPdfData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneratePolicyPdfUseCase {

    private final PdfGeneratorService pdfGeneratorService;

    public byte[] execute(PolicyPdfData data) {
        log.info("Generating policy PDF for: {}", data.getPolicyNumber());
        return pdfGeneratorService.generatePolicyPdf(data);
    }
}