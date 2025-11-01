package com.safetrip.backend.web.rest;

import com.safetrip.backend.application.usecase.GeneratePolicyPdfUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
@Slf4j
public class PdfController {

    private final GeneratePolicyPdfUseCase generatePolicyPdfUseCase;

    /**
     * Genera y descarga el PDF de una póliza
     *
     * @param policyId ID de la póliza
     * @return PDF como byte array con headers apropiados
     */
    @GetMapping("/policy/{policyId}")
    public ResponseEntity<byte[]> generatePolicyPdf(@PathVariable Long policyId) {
        log.info("📄 Solicitud de generación de PDF para póliza: {}", policyId);

        try {
            // Generar PDF
            byte[] pdfBytes = generatePolicyPdfUseCase.execute(policyId);

            // Configurar headers para la descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData(
                    "attachment",
                    String.format("poliza-%d.pdf", policyId)
            );
            headers.setContentLength(pdfBytes.length);
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);

            log.info("✅ PDF generado exitosamente para póliza: {} ({} bytes)",
                    policyId, pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            log.error("❌ Póliza no encontrada: {}", policyId);
            return ResponseEntity.notFound().build();

        } catch (SecurityException e) {
            log.error("❌ Usuario no autorizado para póliza: {}", policyId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (Exception e) {
            log.error("❌ Error generando PDF para póliza {}: {}",
                    policyId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint alternativo para previsualizar el PDF en el navegador (inline)
     */
    @GetMapping("/policy/{policyId}/preview")
    public ResponseEntity<byte[]> previewPolicyPdf(@PathVariable Long policyId) {
        log.info("👁️ Solicitud de previsualización de PDF para póliza: {}", policyId);

        try {
            byte[] pdfBytes = generatePolicyPdfUseCase.execute(policyId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            // Usar "inline" para mostrar en el navegador en lugar de descargar
            headers.add("Content-Disposition",
                    String.format("inline; filename=\"poliza-%d.pdf\"", policyId));
            headers.setContentLength(pdfBytes.length);

            log.info("✅ PDF previsualizado exitosamente para póliza: {}", policyId);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            log.error("❌ Póliza no encontrada: {}", policyId);
            return ResponseEntity.notFound().build();

        } catch (SecurityException e) {
            log.error("❌ Usuario no autorizado para póliza: {}", policyId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (Exception e) {
            log.error("❌ Error generando PDF para póliza {}: {}",
                    policyId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}