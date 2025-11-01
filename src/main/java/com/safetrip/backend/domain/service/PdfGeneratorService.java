package com.safetrip.backend.domain.service;

import com.safetrip.backend.infrastructure.integration.pdf.dto.Template01ApData;
import com.safetrip.backend.infrastructure.integration.pdf.dto.Template02ApData;
import java.io.OutputStream;

public interface PdfGeneratorService {

    /**
     * Genera un PDF usando Template01Ap y retorna los bytes
     * @param data Datos de la póliza
     * @return Bytes del PDF generado
     */
    byte[] generateTemplate01Pdf(Template01ApData data);

    /**
     * Genera un PDF usando Template01Ap y lo escribe en el outputStream
     * @param data Datos de la póliza
     * @param outputStream Stream donde se escribirá el PDF
     */
    void generateTemplate01Pdf(Template01ApData data, OutputStream outputStream);

    /**
     * Genera un PDF usando Template02Ap para una página de asegurados
     * @param pageData Datos de la página
     * @param outputStream Stream donde se escribirá el PDF
     */
    void generateTemplate02Pdf(Template02ApData.PageData pageData, OutputStream outputStream);

    /**
     * Genera un PDF completo combinando Template01 y múltiples Template02
     * @param template01Data Datos del Template01
     * @param template02Data Datos del Template02 (lista completa de asegurados)
     * @return Bytes del PDF combinado
     */
    byte[] generateCompletePolicyPdf(Template01ApData template01Data, Template02ApData template02Data);
}
