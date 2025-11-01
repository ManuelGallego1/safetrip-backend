package com.safetrip.backend.infrastructure.integration.pdf;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.kernel.utils.PdfMerger;
import com.safetrip.backend.domain.service.PdfGeneratorService;
import com.safetrip.backend.infrastructure.integration.pdf.config.PdfConfig;
import com.safetrip.backend.infrastructure.integration.pdf.dto.Template01ApData;
import com.safetrip.backend.infrastructure.integration.pdf.dto.Template02ApData;
import com.safetrip.backend.infrastructure.integration.pdf.mapper.Template02ApMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ITextPdfGenerator implements PdfGeneratorService {

    private final PdfConfig pdfConfig;
    private final Template02ApMapper template02ApMapper;

    // Cachear los bytes de las fuentes, NO las instancias de PdfFont
    private byte[] openSansFontBytes;
    private byte[] openSansBoldFontBytes;

    private void loadFontBytes() throws IOException {
        if (openSansFontBytes == null) {
            ClassPathResource regularFont = new ClassPathResource("fonts/OpenSans-Regular.ttf");
            ClassPathResource boldFont = new ClassPathResource("fonts/OpenSans-Bold.ttf");

            openSansFontBytes = regularFont.getInputStream().readAllBytes();
            openSansBoldFontBytes = boldFont.getInputStream().readAllBytes();

            log.info("Open Sans font bytes loaded successfully");
        }
    }

    private PdfFont createOpenSansFont() throws IOException {
        loadFontBytes();
        return PdfFontFactory.createFont(
                openSansFontBytes,
                PdfEncodings.IDENTITY_H,
                PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
        );
    }

    private PdfFont createOpenSansBoldFont() throws IOException {
        loadFontBytes();
        return PdfFontFactory.createFont(
                openSansBoldFontBytes,
                PdfEncodings.IDENTITY_H,
                PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
        );
    }

    @Override
    public byte[] generateTemplate01Pdf(Template01ApData data) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            generateTemplate01Pdf(data, baos);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Error generating Template01 PDF", e);
            throw new RuntimeException("Failed to generate Template01 PDF", e);
        }
    }

    @Override
    public void generateTemplate01Pdf(Template01ApData data, OutputStream outputStream) {
        PdfDocument pdfDoc = null;
        try {
            // Crear nuevas instancias de fuente para CADA PDF
            PdfFont openSansFont = createOpenSansFont();
            PdfFont openSansBoldFont = createOpenSansBoldFont();

            // Usar PdfDocument con stamping mode para modificar el PDF existente
            PdfReader reader = new PdfReader(pdfConfig.getTemplate01Resource().getInputStream());
            PdfWriter writer = new PdfWriter(outputStream);

            // Usar StampingProperties para modo de modificación
            pdfDoc = new PdfDocument(reader, writer, new StampingProperties());

            PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, false);

            if (form == null) {
                log.error("No form fields found in PDF template");
                throw new RuntimeException("PDF template does not contain form fields");
            }

            // Configurar las fuentes por defecto del formulario antes de llenar
            form.setGenerateAppearance(true);

            // Llenar los campos del formulario
            fillTemplate01FormFields(form, data, openSansFont, openSansBoldFont);

            // Aplanar el formulario DESPUÉS de configurar todo
            form.flattenFields();

            log.info("Template01 PDF generated successfully for policy: {}", data.getNumPoliza());

        } catch (IOException e) {
            log.error("Error processing PDF template", e);
            throw new RuntimeException("Failed to process PDF template", e);
        } finally {
            if (pdfDoc != null) {
                pdfDoc.close();
            }
        }
    }

    private void fillTemplate01FormFields(PdfAcroForm form, Template01ApData data,
                                          PdfFont openSansFont, PdfFont openSansBoldFont) {
        Map<String, PdfFormField> fields = form.getFormFields();

        // Log de campos disponibles en el PDF (útil para debugging)
        log.debug("Available form fields: {}", fields.keySet());

        // Número de póliza
        setFieldValue(form, "numPoliza", data.getNumPoliza(), openSansBoldFont, 20f, new DeviceRgb(Color.WHITE) , PdfFormField.ALIGN_CENTER);
        setFieldValue(form, "numPoliza2", data.getNumPoliza(), openSansFont, 20f, null, PdfFormField.ALIGN_LEFT);

        // Datos del tomador
        setFieldValue(form, "nomTomador", data.getNomTomador(), openSansBoldFont, 20f, new DeviceRgb(Color.WHITE) , PdfFormField.ALIGN_CENTER);
        setFieldValue(form, "nomTomador2", data.getNomTomador(), openSansFont, 20f,  null, PdfFormField.ALIGN_LEFT);
        setFieldValue(form, "dir", data.getDir(), openSansFont, 20f, null, PdfFormField.ALIGN_LEFT);
        setFieldValue(form, "celular", data.getCelular(), openSansFont, 20f, null, PdfFormField.ALIGN_LEFT);

        // Tipo de documento - BOLD
        setFieldValue(form, "tipoDoc", data.getTipoDoc(), openSansBoldFont, 24f,  null, PdfFormField.ALIGN_LEFT);
        setFieldValue(form, "numDoc", data.getNumDoc(), openSansFont, 20f,  null, PdfFormField.ALIGN_LEFT);

        // Número de asegurados
        setFieldValue(form, "numAsegurados", data.getNumAsegurados(), openSansFont, 20f, new DeviceRgb(255, 67, 103), PdfFormField.ALIGN_CENTER);

        // Fecha de expedición
        setFieldValue(form, "hExp", formatHour(data.getHExp()), openSansFont, 16f, null, PdfFormField.ALIGN_CENTER);
        setFieldValue(form, "diaExp", data.getDiaExp(), openSansFont, 16f,null, PdfFormField.ALIGN_CENTER);
        setFieldValue(form, "mesExp", data.getMesExp(), openSansFont, 16f,null, PdfFormField.ALIGN_CENTER);
        setFieldValue(form, "anExp", data.getAnExp(), openSansFont, 16f,null, PdfFormField.ALIGN_CENTER);

        // Fecha desde
        setFieldValue(form, "hDesde", formatHour(data.getHDesde()), openSansFont, 16f,null, PdfFormField.ALIGN_CENTER);
        setFieldValue(form, "diaDesde", data.getDiaDesde(), openSansFont, 16f, new DeviceRgb(255, 67, 103), PdfFormField.ALIGN_CENTER);
        setFieldValue(form, "mesDesde", data.getMesDesde(), openSansFont, 16f,new DeviceRgb(255, 67, 103), PdfFormField.ALIGN_CENTER);
        setFieldValue(form, "anDesde", data.getAnDesde(), openSansFont, 16f,null, PdfFormField.ALIGN_CENTER);

        // Fecha hasta
        setFieldValue(form, "diaHasta", data.getDiaHasta(), openSansFont, 16f,new DeviceRgb(255, 67, 103), PdfFormField.ALIGN_CENTER);
        setFieldValue(form, "mesHasta", data.getMesHasta(), openSansFont, 16f,new DeviceRgb(255, 67, 103), PdfFormField.ALIGN_CENTER);
        setFieldValue(form, "anHasta", data.getAnHasta(), openSansFont, 16f,null, PdfFormField.ALIGN_CENTER);

        // Valor de la póliza
        setFieldValue(form, "valor", data.getValor(), openSansBoldFont, 24f, new DeviceRgb(255, 67, 103), PdfFormField.ALIGN_CENTER);

        // Teléfono de emergencias
        setFieldValue(form, "telEmer", data.getTelEmer(), openSansBoldFont, 32f, null, PdfFormField.ALIGN_CENTER);
    }

    /**
     * Método completo para setear valores con color y alineación personalizados
     */
    private void setFieldValue(PdfAcroForm form, String fieldName, Object value,
                               PdfFont font, float fontSize, DeviceRgb color, int alignment) {
        if (value == null) {
            return;
        }

        try {
            PdfFormField field = form.getField(fieldName);
            if (field != null) {
                // Configurar fuente y tamaño
                field.setFont(font);
                field.setFontSize(fontSize);

                // Configurar color si se especifica
                if (color != null) {
                    field.setColor(color);
                }

                // Configurar alineación
                field.setJustification(alignment);

                // Establecer el valor
                field.setValue(String.valueOf(value));

                // Regenerar apariencia
                field.regenerateField();

                log.debug("Set field '{}' to value '{}' with font size {}, alignment {}",
                        fieldName, value, fontSize, alignment);
            } else {
                log.warn("Form field '{}' not found in PDF template", fieldName);
            }
        } catch (Exception e) {
            log.error("Error setting field '{}' to value '{}'", fieldName, value, e);
        }
    }

    /**
     * Formatea la hora al formato HH:mm (24 horas)
     * Ejemplo: "9" -> "09:00", "15" -> "15:00"
     */
    private String formatHour(String hour) {
        if (hour == null || hour.trim().isEmpty()) {
            return null;
        }
        try {
            int hourInt = Integer.parseInt(hour.trim());
            return String.format("%02d:00", hourInt);
        } catch (NumberFormatException e) {
            log.warn("Invalid hour format: {}", hour);
            return hour; // Retornar el valor original si no se puede parsear
        }
    }

    // ==================== MÉTODOS PARA TEMPLATE02AP ====================

    @Override
    public void generateTemplate02Pdf(Template02ApData.PageData pageData, OutputStream outputStream) {
        PdfDocument pdfDoc = null;
        try {
            // Crear fuentes
            PdfFont openSansFont = createOpenSansFont();
            PdfFont openSansBoldFont = createOpenSansBoldFont();

            // Leer template02Ap.pdf
            PdfReader reader = new PdfReader(pdfConfig.getTemplate02Resource().getInputStream());
            PdfWriter writer = new PdfWriter(outputStream);

            pdfDoc = new PdfDocument(reader, writer, new StampingProperties());

            PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, false);

            if (form == null) {
                log.error("No form fields found in Template02 PDF");
                throw new RuntimeException("Template02 PDF does not contain form fields");
            }

            form.setGenerateAppearance(true);

            // Llenar campos del formulario
            fillTemplate02FormFields(form, pageData, openSansFont, openSansBoldFont);

            // Aplanar formulario
            form.flattenFields();

            log.info("Template02 PDF generated successfully - Page {}/{}",
                    pageData.getPageNumber(), pageData.getTotalPages());

        } catch (IOException e) {
            log.error("Error processing Template02 PDF", e);
            throw new RuntimeException("Failed to process Template02 PDF", e);
        } finally {
            if (pdfDoc != null) {
                pdfDoc.close();
            }
        }
    }

    private void fillTemplate02FormFields(PdfAcroForm form, Template02ApData.PageData pageData,
                                          PdfFont openSansFont, PdfFont openSansBoldFont) {
        Map<String, PdfFormField> fields = form.getFormFields();

        log.debug("Available form fields in Template02: {}", fields.keySet());

        // Número de póliza con estilo especial (blanco, centrado, bold)
        setFieldValue(form, "numPoliza", pageData.getNumPoliza(), openSansBoldFont, 20f,
                new DeviceRgb(255, 255, 255), PdfFormField.ALIGN_CENTER);
        setFieldValue(form, "numPoliza2", pageData.getNumPoliza(), openSansBoldFont, 16f,
                new DeviceRgb(255, 255, 255), PdfFormField.ALIGN_LEFT);

        // Valor con estilo especial (rosa/rojo, centrado, bold)
        setFieldValue(form, "valor", pageData.getValor(), openSansBoldFont, 32f,
                new DeviceRgb(255, 67, 103), PdfFormField.ALIGN_CENTER);

        // Llenar párrafo de nombres (nom1 contendrá todos los nombres con saltos de línea)
        List<String> nombres = pageData.getNombres();
        if (!nombres.isEmpty()) {
            String nombresTexto = String.join("\n", nombres);
            setFieldValue(form, "nombres", nombresTexto, openSansFont, 16f, null, PdfFormField.ALIGN_CENTER);
            log.debug("✅ Párrafo de nombres: {} líneas", nombres.size());
        }

        // Llenar párrafo de documentos (doc1 contendrá todos los documentos con saltos de línea)
        List<String> documentos = pageData.getDocumentos();
        if (!documentos.isEmpty()) {
            String documentosTexto = String.join("\n", documentos);
            setFieldValue(form, "documentos", documentosTexto, openSansFont, 16f, null, PdfFormField.ALIGN_CENTER);
            log.debug("✅ Párrafo de documentos: {} líneas", documentos.size());
        }

        log.debug("✅ Template02 completado con {} asegurados", nombres.size());
    }

    @Override
    public byte[] generateCompletePolicyPdf(Template01ApData template01Data, Template02ApData template02Data) {
        try (ByteArrayOutputStream resultStream = new ByteArrayOutputStream()) {

            log.info("🔄 Generando PDF completo de póliza...");

            // 1. Generar Template01
            ByteArrayOutputStream template01Stream = new ByteArrayOutputStream();
            generateTemplate01Pdf(template01Data, template01Stream);
            byte[] template01Bytes = template01Stream.toByteArray();

            log.debug("✅ Template01 generado: {} bytes", template01Bytes.length);

            // 2. Dividir asegurados en páginas (máximo 20 por página)
            List<Template02ApData.PageData> pages = template02ApMapper.splitIntoPages(template02Data);

            log.debug("📄 Se generarán {} página(s) de Template02", pages.size());

            // 3. Generar Template02 para cada página
            List<byte[]> template02BytesList = new java.util.ArrayList<>();
            for (Template02ApData.PageData page : pages) {
                ByteArrayOutputStream pageStream = new ByteArrayOutputStream();
                generateTemplate02Pdf(page, pageStream);
                template02BytesList.add(pageStream.toByteArray());

                log.debug("✅ Template02 página {}/{} generado: {} bytes",
                        page.getPageNumber(), page.getTotalPages(), pageStream.size());
            }

            // 4. Combinar todos los PDFs
            PdfDocument resultPdf = new PdfDocument(new PdfWriter(resultStream));
            PdfMerger merger = new PdfMerger(resultPdf);

            // Agregar Template01
            PdfDocument template01Pdf = new PdfDocument(new PdfReader(new ByteArrayInputStream(template01Bytes)));
            merger.merge(template01Pdf, 1, template01Pdf.getNumberOfPages());
            template01Pdf.close();

            log.debug("✅ Template01 añadido al PDF final");

            // Agregar todos los Template02
            for (int i = 0; i < template02BytesList.size(); i++) {
                PdfDocument template02Pdf = new PdfDocument(
                        new PdfReader(new ByteArrayInputStream(template02BytesList.get(i)))
                );
                merger.merge(template02Pdf, 1, template02Pdf.getNumberOfPages());
                template02Pdf.close();

                log.debug("✅ Template02 página {} añadido al PDF final", i + 1);
            }

            resultPdf.close();

            byte[] finalBytes = resultStream.toByteArray();
            log.info("🎉 PDF completo generado exitosamente: {} bytes totales", finalBytes.length);

            return finalBytes;

        } catch (IOException e) {
            log.error("❌ Error generando PDF completo", e);
            throw new RuntimeException("Failed to generate complete policy PDF", e);
        }
    }
}