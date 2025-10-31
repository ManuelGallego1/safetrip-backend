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
import com.safetrip.backend.domain.service.PdfGeneratorService;
import com.safetrip.backend.infrastructure.integration.pdf.config.PdfConfig;
import com.safetrip.backend.infrastructure.integration.pdf.dto.PolicyPdfData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ITextPdfGenerator implements PdfGeneratorService {

    private final PdfConfig pdfConfig;

    // Color blanco
    private static final DeviceRgb WHITE_COLOR = new DeviceRgb(255, 255, 255);

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
    public byte[] generatePolicyPdf(PolicyPdfData data) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            generatePolicyPdf(data, baos);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Error generating policy PDF", e);
            throw new RuntimeException("Failed to generate policy PDF", e);
        }
    }

    @Override
    public void generatePolicyPdf(PolicyPdfData data, OutputStream outputStream) {
        PdfDocument pdfDoc = null;
        try {
            // Crear nuevas instancias de fuente para CADA PDF
            PdfFont openSansFont = createOpenSansFont();
            PdfFont openSansBoldFont = createOpenSansBoldFont();

            // Usar PdfDocument con stamping mode para modificar el PDF existente
            PdfReader reader = new PdfReader(pdfConfig.getPolicyTemplateResource().getInputStream());
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
            fillFormFields(form, data, openSansFont, openSansBoldFont);

            // Aplanar el formulario DESPUÉS de configurar todo
            form.flattenFields();

            log.info("Policy PDF generated successfully for policy: {}", data.getPolicyNumber());

        } catch (IOException e) {
            log.error("Error processing PDF template", e);
            throw new RuntimeException("Failed to process PDF template", e);
        } finally {
            if (pdfDoc != null) {
                pdfDoc.close();
            }
        }
    }

    private void fillFormFields(PdfAcroForm form, PolicyPdfData data, PdfFont openSansFont, PdfFont openSansBoldFont) {
        Map<String, PdfFormField> fields = form.getFormFields();

        // Log de campos disponibles en el PDF (útil para debugging)
        log.debug("Available form fields: {}", fields.keySet());

        // Policy Number - Bold, 25px, BLANCO
        setFieldValue(form, "policy_number", data.getPolicyNumber(), openSansBoldFont, 24f, WHITE_COLOR);

        // Collective Policy - Pequeño, 8px, BLANCO
        setFieldValue(form, "collective_policy", data.getCollectivePolicy(), openSansFont, 8f, WHITE_COLOR);

        // Name Hotel - Bold, 16px, BLANCO
        setFieldValue(form, "name_hotel", data.getNameHotel(), openSansBoldFont, 20f, WHITE_COLOR);

        // Campos normales - 16px, sin color (negro por defecto)
        setFieldValue(form, "name_hotel2", data.getNameHotel2(), openSansFont, 20f, null);
        setFieldValue(form, "nit", data.getNit(), openSansFont, 20f, null);
        setFieldValue(form, "phone_number", data.getPhoneNumber(), openSansFont, 20f, null);
        setFieldValue(form, "address", data.getAddress(), openSansFont, 20f, null);
        setFieldValue(form, "person_count", data.getPersonCount(), openSansFont, 20f, null);

        // Fecha y hora de expedición - 16px (hora formateada)
        setFieldValue(form, "hour", formatHour(data.getHour()), openSansFont, 16f, null);
        setFieldValue(form, "day", data.getDay(), openSansFont, 16f, null);
        setFieldValue(form, "month", data.getMonth(), openSansFont, 16f, null);
        setFieldValue(form, "year", data.getYear(), openSansFont, 16f, null);

        // Fecha desde - 16px (hora formateada)
        setFieldValue(form, "hour_from", formatHour(data.getHourFrom()), openSansFont, 16f, null);
        setFieldValue(form, "day_from", data.getDayFrom(), openSansFont, 16f, null);
        setFieldValue(form, "month_from", data.getMonthFrom(), openSansFont, 16f, null);
        setFieldValue(form, "year_from", data.getYearFrom(), openSansFont, 16f, null);

        // Fecha hasta - 16px
        setFieldValue(form, "day_to", data.getDayTo(), openSansFont, 16f, null);
        setFieldValue(form, "month_to", data.getMonthTo(), openSansFont, 16f, null);
        setFieldValue(form, "year_to", data.getYearTo(), openSansFont, 16f, null);

        // Value Policy - 16px
        setFieldValue(form, "value_policy", data.getValuePolicy(), openSansFont, 18f, null);
    }

    private void setFieldValue(PdfAcroForm form, String fieldName, Object value, PdfFont font, float fontSize, DeviceRgb color) {
        if (value == null) {
            return;
        }

        try {
            PdfFormField field = form.getField(fieldName);
            if (field != null) {
                // Primero configurar fuente y color
                field.setFont(font);
                field.setFontSize(fontSize);

                // Aplicar color si se especifica
                if (color != null) {
                    field.setColor(color);
                }

                // Luego establecer el valor
                field.setValue(String.valueOf(value));

                // Regenerar apariencia
                field.regenerateField();

                log.debug("Set field '{}' to value '{}' with font size {} and color {}",
                        fieldName, value, fontSize, color != null ? "white" : "default");
            } else {
                log.warn("Form field '{}' not found in PDF template", fieldName);
            }
        } catch (Exception e) {
            log.error("Error setting field '{}' to value '{}'", fieldName, value, e);
        }
    }

    /**
     * Formatea la hora al formato HH:mm (24 horas)
     * Ejemplo: 9 -> "09:00", 15 -> "15:00"
     */
    private String formatHour(Integer hour) {
        if (hour == null) {
            return null;
        }
        return String.format("%02d:00", hour);
    }
}