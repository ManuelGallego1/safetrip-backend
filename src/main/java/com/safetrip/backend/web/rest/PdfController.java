package com.safetrip.backend.web.rest;

import com.safetrip.backend.application.usecase.GeneratePolicyPdfUseCase;
import com.safetrip.backend.infrastructure.integration.pdf.dto.PolicyPdfData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfController {

    private final GeneratePolicyPdfUseCase generatePolicyPdfUseCase;

    @PostMapping("/policy")
    public ResponseEntity<byte[]> generatePolicyPdf(@RequestBody PolicyPdfData data) {
        byte[] pdfBytes = generatePolicyPdfUseCase.execute(data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                "policy-" + data.getPolicyNumber() + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}