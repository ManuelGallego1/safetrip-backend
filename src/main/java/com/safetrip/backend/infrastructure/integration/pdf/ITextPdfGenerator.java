package com.safetrip.backend.infrastructure.integration.pdf;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.annot.PdfWidgetAnnotation;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.kernel.utils.PdfMerger;
import com.safetrip.backend.domain.service.PdfGeneratorService;
import com.safetrip.backend.infrastructure.integration.notification.resend.dto.EmailAttachment;
import com.safetrip.backend.infrastructure.integration.pdf.config.PdfConfig;
import com.safetrip.backend.infrastructure.integration.pdf.dto.Template01ApData;
import com.safetrip.backend.infrastructure.integration.pdf.dto.Template02ApData;
import com.safetrip.backend.infrastructure.integration.pdf.mapper.Template02ApMapper;
import com.safetrip.backend.infrastructure.integration.pdf.service.TemplatePaginator;
import com.safetrip.backend.infrastructure.integration.pdf.utils.DateTimeUtils;
import com.safetrip.backend.infrastructure.integration.pdf.utils.DateTimeUtils.DateTimeComponents;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import com.itextpdf.kernel.geom.Rectangle;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ITextPdfGenerator implements PdfGeneratorService {

    private final PdfConfig pdfConfig;
    private final Template02ApMapper template02ApMapper;
    private final TemplatePaginator templatePaginator;

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

    /**
     * Selecciona el resource del template02 correcto según el tipo de póliza
     * @param tipo 1L = template02Ap.pdf (sin fechas), 2L = template02Sh.pdf (con fechas)
     * @return Resource del template correspondiente
     */
    private Resource selectTemplate02Resource(Long tipo) {
        if (tipo == null) {
            log.warn("⚠️ Tipo de póliza null, usando template02 por defecto (Ap)");
            return pdfConfig.getTemplate02Resource();
        }

        switch (tipo.intValue()) {
            case 1:
                log.debug("✅ Tipo 1: Usando template02Ap.pdf (sin fechas)");
                return pdfConfig.getTemplate02Resource();

            case 2:
                log.debug("✅ Tipo 2: Usando template02Sh.pdf (con fechas)");
                return pdfConfig.getTemplate02ShResource();

            case 3:
                log.debug("✅ Tipo 3: Usando template02ShPref.pdf (preferencial - inominada)");
                return pdfConfig.getTemplate02ShPrefResource();
            case 4:
                log.debug("✅ Tipo 4: Usando template02ApPax.pdf (variante visual de Ap)");
                return pdfConfig.getTemplate02ApPaxResource();

            default:
                log.warn("⚠️ Tipo de póliza desconocido ({}), usando template02 por defecto (Ap)", tipo);
                return pdfConfig.getTemplate02Resource();
        }
    }

    private void fillTemplate02PreferentialFields(PdfAcroForm form, Template02ApData.PageData pageData,
                                                  PdfFont openSansFont, PdfFont openSansBoldFont) {
        Map<String, PdfFormField> fields = form.getFormFields();
        log.debug("Llenando campos preferenciales de Template02 (tipo 3): {}", fields.keySet());

        // Número de póliza
        setFieldValue(form, "numPoliza", pageData.getNumPoliza(), openSansBoldFont, 20f,
                new DeviceRgb(255, 255, 255), PdfFormField.ALIGN_CENTER);
        setFieldValue(form, "numPoliza2", pageData.getNumPoliza(), openSansBoldFont, 16f,
                new DeviceRgb(255, 255, 255), PdfFormField.ALIGN_LEFT);
        setFieldValue(form, "numPoliza3", pageData.getNumPoliza(), openSansBoldFont, 11f,
                null, PdfFormField.ALIGN_LEFT);
        setFieldValue(form, "numPoliza4", pageData.getNumPoliza(), openSansBoldFont, 11f,
                null, PdfFormField.ALIGN_LEFT);

        // Valor - Color azul para tipo 3 (#4168FF)
        setFieldValue(form, "valor", pageData.getValor(), openSansBoldFont, 32f,
                new DeviceRgb(65, 104, 255), // Color azul para tipo 3
                PdfFormField.ALIGN_CENTER);

        setFieldValue(form, "nomPlan", pageData.getPlan(), openSansBoldFont, 24f,
                    null, PdfFormField.ALIGN_CENTER);
        log.debug("✅ Plan establecido: {}", pageData.getPlan());

        log.debug("✅ Template02 preferencial completado (póliza inominada tipo 3)");
    }

    @Override
    public void generateTemplate01Pdf(Template01ApData data, OutputStream outputStream) {
        PdfDocument pdfDoc = null;
        try {
            // Crear nuevas instancias de fuente para CADA PDF
            PdfFont openSansFont = createOpenSansFont();
            PdfFont openSansBoldFont = createOpenSansBoldFont();

            // 🔥 SELECCIONAR TEMPLATE SEGÚN EL TIPO
            Resource templateResource = selectTemplate01Resource(data.getTipo());

            log.debug("📄 Usando template para tipo {}: {}",
                    data.getTipo(),
                    templateResource.getFilename());

            // Usar PdfDocument con stamping mode para modificar el PDF existente
            PdfReader reader = new PdfReader(templateResource.getInputStream());
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

            log.info("Template01 PDF generated successfully for policy: {} (tipo: {})",
                    data.getNumPoliza(), data.getTipo());

        } catch (IOException e) {
            log.error("Error processing PDF template", e);
            throw new RuntimeException("Failed to process PDF template", e);
        } finally {
            if (pdfDoc != null) {
                pdfDoc.close();
            }
        }
    }

    /**
     * Selecciona el resource del template01 correcto según el tipo de póliza
     * @param tipo 1L = template01Ap.pdf, 2L = template01Sh.pdf
     * @return Resource del template correspondiente
     */
    private Resource selectTemplate01Resource(Long tipo) {
        if (tipo == null) {
            log.warn("⚠️ Tipo de póliza null, usando template por defecto (Ap)");
            return pdfConfig.getTemplate01Resource();
        }

        switch (tipo.intValue()) {
            case 1:
                log.debug("✅ Tipo 1: Usando template01Ap.pdf");
                return pdfConfig.getTemplate01Resource();

            case 2:
                log.debug("✅ Tipo 2: Usando template01Sh.pdf");
                return pdfConfig.getTemplate01ShResource();

            case 3:
                log.debug("✅ Tipo 3: Usando template01Sh.pdf");
                return pdfConfig.getTemplate01ShResource();

            case 4:
                log.debug("✅ Tipo 4: Usando template01ApPax.pdf (variante visual de Ap)");
                return pdfConfig.getTemplate01ApPaxResource();

            default:
                log.warn("⚠️ Tipo de póliza desconocido ({}), usando template por defecto (Ap)", tipo);
                return pdfConfig.getTemplate01Resource();
        }
    }

    /**
     * 🌎 ACTUALIZADO: Llenar campos con conversión automática de UTC a zona horaria Colombia
     * SIEMPRE usa los Instant (fechaExpedicion, fechaDesde, fechaHasta) que vienen en UTC
     */
    private void fillTemplate01FormFields(PdfAcroForm form, Template01ApData data,
                                          PdfFont openSansFont, PdfFont openSansBoldFont) {
        Map<String, PdfFormField> fields = form.getFormFields();
        log.debug("Available form fields: {}", fields.keySet());

        // Número de póliza
        setFieldValue(form, "numPoliza", data.getNumPoliza(), openSansBoldFont, 20f,
                new DeviceRgb(Color.WHITE), PdfFormField.ALIGN_CENTER);
        setFieldValue(form, "numPoliza2", data.getNumPoliza(), openSansFont, 20f,
                null, PdfFormField.ALIGN_LEFT);

        // Datos del tomador
        setFieldValue(form, "nomTomador", data.getNomTomador(), openSansBoldFont, 20f,
                new DeviceRgb(Color.WHITE), PdfFormField.ALIGN_CENTER);
        setFieldValue(form, "nomTomador2", data.getNomTomador(), openSansFont, 20f,
                null, PdfFormField.ALIGN_LEFT);
        setFieldValue(form, "dir", data.getDir(), openSansFont, 20f,
                null, PdfFormField.ALIGN_LEFT);
        setFieldValue(form, "celular", data.getCelular(), openSansFont, 20f,
                null, PdfFormField.ALIGN_LEFT);

        // Tipo de documento - BOLD
        setFieldValue(form, "tipoDoc", data.getTipoDoc(), openSansBoldFont, 24f,
                null, PdfFormField.ALIGN_LEFT);
        setFieldValue(form, "numDoc", data.getNumDoc(), openSansFont, 20f,
                null, PdfFormField.ALIGN_LEFT);

        // Número de asegurados - CAMBIO DE COLOR SEGÚN TIPO
        String numAseguradosText = data.getNumAsegurados();
        if (data.getTipo() != null && data.getTipo() == 3L) {
            numAseguradosText = "INNOMINADO";
            log.debug("🏨 Tipo 3: Campo numAsegurados = 'INNOMINADO'");
        }

        setFieldValue(form, "numAsegurados", numAseguradosText, openSansFont, 20f,
                getColorByType(data.getTipo(), new DeviceRgb(255, 67, 103)),
                PdfFormField.ALIGN_CENTER);

        // 🌎 FECHAS YA VIENEN EN ZONA COLOMBIA DESDE EL MAPPER
        // Solo necesitamos extraer los componentes SIN convertir zona horaria

        String horaExpedicion = null;
        String horaInicio = null;

        // Fecha de expedición - YA ESTÁ EN COLOMBIA
        if (data.getFechaExpedicion() != null) {
            ZonedDateTime fechaExp = data.getFechaExpedicion();
            horaExpedicion = String.format("%02d:00", fechaExp.getHour());

            setFieldValue(form, "hExp", horaExpedicion, openSansFont, 16f,
                    null, PdfFormField.ALIGN_CENTER);
            setFieldValue(form, "diaExp", String.format("%02d", fechaExp.getDayOfMonth()),
                    openSansFont, 16f, null, PdfFormField.ALIGN_CENTER);
            setFieldValue(form, "mesExp", String.format("%02d", fechaExp.getMonthValue()),
                    openSansFont, 16f, null, PdfFormField.ALIGN_CENTER);
            setFieldValue(form, "anExp", String.valueOf(fechaExp.getYear()),
                    openSansFont, 16f, null, PdfFormField.ALIGN_CENTER);

            log.debug("✅ Fecha expedición (Colombia): {} {}/{}/{}",
                    horaExpedicion, fechaExp.getDayOfMonth(),
                    fechaExp.getMonthValue(), fechaExp.getYear());
        } else {
            log.warn("⚠️ fechaExpedicion es null, usando valores por defecto del DTO");
            horaExpedicion = DateTimeUtils.formatHourString(data.getHExp());
            setFieldValue(form, "hExp", horaExpedicion,
                    openSansFont, 16f, null, PdfFormField.ALIGN_CENTER);
            setFieldValue(form, "diaExp", data.getDiaExp(), openSansFont, 16f,
                    null, PdfFormField.ALIGN_CENTER);
            setFieldValue(form, "mesExp", data.getMesExp(), openSansFont, 16f,
                    null, PdfFormField.ALIGN_CENTER);
            setFieldValue(form, "anExp", data.getAnExp(), openSansFont, 16f,
                    null, PdfFormField.ALIGN_CENTER);
        }

        // Fecha desde - YA ESTÁ EN COLOMBIA CON LA HORA CORRECTA
        if (data.getFechaDesde() != null) {
            ZonedDateTime fechaDesde = data.getFechaDesde();
            horaInicio = String.format("%02d:00", fechaDesde.getHour());

            setFieldValue(form, "hDesde", horaInicio, openSansFont, 16f,
                    null, PdfFormField.ALIGN_CENTER);
            setFieldValue(form, "diaDesde", String.format("%02d", fechaDesde.getDayOfMonth()),
                    openSansFont, 16f,
                    getColorByType(data.getTipo(), new DeviceRgb(255, 67, 103)),
                    PdfFormField.ALIGN_CENTER);
            setFieldValue(form, "mesDesde", String.format("%02d", fechaDesde.getMonthValue()),
                    openSansFont, 16f,
                    getColorByType(data.getTipo(), new DeviceRgb(255, 67, 103)),
                    PdfFormField.ALIGN_CENTER);
            setFieldValue(form, "anDesde", String.valueOf(fechaDesde.getYear()),
                    openSansFont, 16f,
                    null, PdfFormField.ALIGN_CENTER);

            setFieldValue(form, "hVigencia", horaInicio, openSansFont, 12f,
                    new DeviceRgb(64, 64, 64), PdfFormField.ALIGN_LEFT);

            log.debug("✅ Fecha desde (Colombia): {} {}/{}/{} (hVigencia: {})",
                    horaInicio, fechaDesde.getDayOfMonth(),
                    fechaDesde.getMonthValue(), fechaDesde.getYear(),
                    data.getHVigencia());
        } else {
            log.warn("⚠️ fechaDesde es null, usando valores por defecto del DTO");
            horaInicio = DateTimeUtils.formatHourString(data.getHDesde());
            setFieldValue(form, "hDesde", horaInicio,
                    openSansFont, 16f, null, PdfFormField.ALIGN_CENTER);
            setFieldValue(form, "diaDesde", data.getDiaDesde(), openSansFont, 16f,
                    getColorByType(data.getTipo(), new DeviceRgb(255, 67, 103)),
                    PdfFormField.ALIGN_CENTER);
            setFieldValue(form, "mesDesde", data.getMesDesde(), openSansFont, 16f,
                    getColorByType(data.getTipo(), new DeviceRgb(255, 67, 103)),
                    PdfFormField.ALIGN_CENTER);
            setFieldValue(form, "anDesde", data.getAnDesde(), openSansFont, 16f,
                    null, PdfFormField.ALIGN_CENTER);

            setFieldValue(form, "hVigencia", horaInicio,
                    openSansFont, 12f, new DeviceRgb(64, 64, 64), PdfFormField.ALIGN_LEFT);
        }

        // Fecha hasta - YA ESTÁ EN COLOMBIA
        if (data.getFechaHasta() != null) {
            ZonedDateTime fechaHasta = data.getFechaHasta();

            setFieldValue(form, "diaHasta", String.format("%02d", fechaHasta.getDayOfMonth()),
                    openSansFont, 16f,
                    getColorByType(data.getTipo(), new DeviceRgb(255, 67, 103)),
                    PdfFormField.ALIGN_CENTER);
            setFieldValue(form, "mesHasta", String.format("%02d", fechaHasta.getMonthValue()),
                    openSansFont, 16f,
                    getColorByType(data.getTipo(), new DeviceRgb(255, 67, 103)),
                    PdfFormField.ALIGN_CENTER);
            setFieldValue(form, "anHasta", String.valueOf(fechaHasta.getYear()),
                    openSansFont, 16f,
                    null, PdfFormField.ALIGN_CENTER);

            log.debug("✅ Fecha hasta (Colombia): {}/{}/{}",
                    fechaHasta.getDayOfMonth(), fechaHasta.getMonthValue(), fechaHasta.getYear());
        } else {
            log.warn("⚠️ fechaHasta es null, usando valores por defecto del DTO");
            setFieldValue(form, "diaHasta", data.getDiaHasta(), openSansFont, 16f,
                    getColorByType(data.getTipo(), new DeviceRgb(255, 67, 103)),
                    PdfFormField.ALIGN_CENTER);
            setFieldValue(form, "mesHasta", data.getMesHasta(), openSansFont, 16f,
                    getColorByType(data.getTipo(), new DeviceRgb(255, 67, 103)),
                    PdfFormField.ALIGN_CENTER);
            setFieldValue(form, "anHasta", data.getAnHasta(), openSansFont, 16f,
                    null, PdfFormField.ALIGN_CENTER);
        }

        // Valor de la póliza - CAMBIO DE COLOR SEGÚN TIPO
        setFieldValue(form, "valor", data.getValor(), openSansBoldFont, 24f,
                getColorByType(data.getTipo(), new DeviceRgb(255, 67, 103)),
                PdfFormField.ALIGN_CENTER);

        // Teléfono de emergencias
        setFieldValue(form, "telEmer", data.getTelEmer(), openSansBoldFont, 32f,
                null, PdfFormField.ALIGN_CENTER);
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

    // ==================== MÉTODOS PARA TEMPLATE02AP ====================

    @Override
    public void generateTemplate02Pdf(Template02ApData.PageData pageData, OutputStream outputStream) {
        PdfDocument pdfDoc = null;
        try {
            // Crear fuentes
            PdfFont openSansFont = createOpenSansFont();
            PdfFont openSansBoldFont = createOpenSansBoldFont();

            // 🔥 SELECCIONAR TEMPLATE SEGÚN TIPO Y SI TIENE ARCHIVOS
            Resource templateResource = selectTemplate02ResourceWithFileSupport(
                    pageData.getTipo(),
                    pageData.getCreatedWithFiles()
            );

            log.debug("📄 Usando template02 para tipo {} (archivos: {}): {}",
                    pageData.getTipo(),
                    pageData.getCreatedWithFiles(),
                    templateResource.getFilename());

            PdfReader reader = new PdfReader(templateResource.getInputStream());
            PdfWriter writer = new PdfWriter(outputStream);

            pdfDoc = new PdfDocument(reader, writer, new StampingProperties());
            PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, false);

            if (form == null) {
                log.error("No form fields found in Template02 PDF");
                throw new RuntimeException("Template02 PDF does not contain form fields");
            }

            form.setGenerateAppearance(true);

            // 🆕 MANEJAR TIPO 3 (INOMINADA - PREFERENCIAL)
            if (Long.valueOf(3L).equals(pageData.getTipo())) {
                fillTemplate02PreferentialFields(form, pageData, openSansFont, openSansBoldFont);
            }
            // Si es template con archivos, solo llenar campos básicos
            else if (Boolean.TRUE.equals(pageData.getCreatedWithFiles())) {
                fillTemplate02BasicFields(form, pageData, openSansFont, openSansBoldFont);
            }
            // Llenar campos completos con lista de asegurados
            else {
                fillTemplate02FormFields(form, pageData, openSansFont, openSansBoldFont);
            }

            form.flattenFields();

            log.info("Template02 PDF generated successfully - Page {}/{} (tipo: {}, archivos: {})",
                    pageData.getPageNumber(), pageData.getTotalPages(),
                    pageData.getTipo(), pageData.getCreatedWithFiles());

        } catch (IOException e) {
            log.error("Error processing Template02 PDF", e);
            throw new RuntimeException("Failed to process Template02 PDF", e);
        } finally {
            if (pdfDoc != null) {
                pdfDoc.close();
            }
        }
    }


    /**
     * Llena solo los campos básicos del template02 (para pólizas con archivos)
     */
    private void fillTemplate02BasicFields(PdfAcroForm form, Template02ApData.PageData pageData,
                                           PdfFont openSansFont, PdfFont openSansBoldFont) {
        Map<String, PdfFormField> fields = form.getFormFields();
        log.debug("Llenando campos básicos de Template02: {}", fields.keySet());

        // Número de póliza
        setFieldValue(form, "numPoliza", pageData.getNumPoliza(), openSansBoldFont, 20f,
                new DeviceRgb(255, 255, 255), PdfFormField.ALIGN_CENTER);
        setFieldValue(form, "numPoliza2", pageData.getNumPoliza(), openSansBoldFont, 16f,
                new DeviceRgb(255, 255, 255), PdfFormField.ALIGN_LEFT);
        setFieldValue(form, "numPoliza3", pageData.getNumPoliza(), openSansBoldFont, 11f,
                null, PdfFormField.ALIGN_LEFT);
        setFieldValue(form, "numPoliza4", pageData.getNumPoliza(), openSansBoldFont, 11f,
                null, PdfFormField.ALIGN_LEFT);

        // Valor
        setFieldValue(form, "valor", pageData.getValor(), openSansBoldFont, 32f,
                getColorByType(pageData.getTipo(), new DeviceRgb(255, 67, 103)),
                PdfFormField.ALIGN_CENTER);

        log.debug("✅ Template02 básico completado (póliza con archivos adjuntos)");
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
        setFieldValue(form, "numPoliza3", pageData.getNumPoliza(), openSansBoldFont, 11f,
                null, PdfFormField.ALIGN_LEFT);
        setFieldValue(form, "numPoliza4", pageData.getNumPoliza(), openSansBoldFont, 11f,
                null, PdfFormField.ALIGN_LEFT);

        // Valor con estilo especial - CAMBIO DE COLOR SEGÚN TIPO
        setFieldValue(form, "valor", pageData.getValor(), openSansBoldFont, 32f,
                getColorByType(pageData.getTipo(), new DeviceRgb(255, 67, 103)), PdfFormField.ALIGN_CENTER);

        // Obtener el ancho del campo de nombres para calcular saltos de línea
        PdfFormField nombresField = fields.get("nombres");
        float fieldWidth = 0f;
        if (nombresField != null && nombresField.getWidgets().size() > 0) {
            PdfWidgetAnnotation widget = nombresField.getWidgets().get(0);
            com.itextpdf.kernel.geom.Rectangle rect = widget.getRectangle().toRectangle();
            fieldWidth = rect.getWidth();
        }

        // Llenar párrafo de nombres y calcular líneas reales que ocupará cada uno
        List<String> nombres = pageData.getNombres();
        List<Integer> lineasPorNombre = new ArrayList<>();

        if (!nombres.isEmpty()) {
            StringBuilder nombresTexto = new StringBuilder();

            for (int i = 0; i < nombres.size(); i++) {
                String nombre = nombres.get(i);
                // Calcular cuántas líneas ocupará este nombre
                int lineas = calcularLineasNecesarias(nombre, openSansFont, 16f, fieldWidth);
                lineasPorNombre.add(lineas);

                nombresTexto.append(nombre);
                if (i < nombres.size() - 1) {
                    nombresTexto.append("\n");
                }
            }

            setFieldValue(form, "nombres", nombresTexto.toString(), openSansFont, 16f,
                    null, PdfFormField.ALIGN_CENTER);
            log.debug("✅ Párrafo de nombres: {} entradas, {} líneas totales",
                    nombres.size(), lineasPorNombre.stream().mapToInt(Integer::intValue).sum());
        }

        // Llenar párrafo de documentos sincronizando con las líneas de nombres
        List<String> documentos = pageData.getDocumentos();
        if (!documentos.isEmpty()) {
            StringBuilder documentosTexto = new StringBuilder();

            for (int i = 0; i < documentos.size(); i++) {
                String documento = documentos.get(i);
                documentosTexto.append(documento);

                // Agregar líneas vacías adicionales si el nombre correspondiente ocupa más de 1 línea
                if (i < lineasPorNombre.size()) {
                    int lineasExtras = lineasPorNombre.get(i) - 1;
                    for (int j = 0; j < lineasExtras; j++) {
                        documentosTexto.append("\n");
                    }
                }

                if (i < documentos.size() - 1) {
                    documentosTexto.append("\n");
                }
            }

            setFieldValue(form, "documentos", documentosTexto.toString(), openSansFont, 16f,
                    null, PdfFormField.ALIGN_CENTER);
            log.debug("✅ Párrafo de documentos sincronizado: {} líneas", documentos.size());
        }

        // Llenar fechas SOLO si es tipo 2, también sincronizadas
        if (pageData.getFechasDesde() != null && pageData.getFechasHasta() != null) {
            List<String> fechasDesde = pageData.getFechasDesde();
            if (!fechasDesde.isEmpty()) {
                StringBuilder fechasDesdeTexto = new StringBuilder();

                for (int i = 0; i < fechasDesde.size(); i++) {
                    String fecha = fechasDesde.get(i);
                    fechasDesdeTexto.append(fecha);

                    // Sincronizar con líneas de nombres
                    if (i < lineasPorNombre.size()) {
                        int lineasExtras = lineasPorNombre.get(i) - 1;
                        for (int j = 0; j < lineasExtras; j++) {
                            fechasDesdeTexto.append("\n");
                        }
                    }

                    if (i < fechasDesde.size() - 1) {
                        fechasDesdeTexto.append("\n");
                    }
                }

                setFieldValue(form, "fechasDesde", fechasDesdeTexto.toString(), openSansFont, 16f,
                        null, PdfFormField.ALIGN_CENTER);
                log.debug("✅ Párrafo de fechas desde sincronizado: {} líneas", fechasDesde.size());
            }

            List<String> fechasHasta = pageData.getFechasHasta();
            if (!fechasHasta.isEmpty()) {
                StringBuilder fechasHastaTexto = new StringBuilder();

                for (int i = 0; i < fechasHasta.size(); i++) {
                    String fecha = fechasHasta.get(i);
                    fechasHastaTexto.append(fecha);

                    // Sincronizar con líneas de nombres
                    if (i < lineasPorNombre.size()) {
                        int lineasExtras = lineasPorNombre.get(i) - 1;
                        for (int j = 0; j < lineasExtras; j++) {
                            fechasHastaTexto.append("\n");
                        }
                    }

                    if (i < fechasHasta.size() - 1) {
                        fechasHastaTexto.append("\n");
                    }
                }

                setFieldValue(form, "hasta", fechasHastaTexto.toString(), openSansFont, 16f,
                        null, PdfFormField.ALIGN_CENTER);
                log.debug("✅ Párrafo de fechas hasta sincronizado: {} líneas", fechasHasta.size());
            }
        } else {
            log.debug("⏭️ Sin fechas para mapear (tipo 1)");
        }

        log.debug("✅ Template02 completado con {} asegurados", nombres.size());
    }

    /**
     * Calcula cuántas líneas necesitará un texto dado el ancho del campo
     */
    private int calcularLineasNecesarias(String texto, PdfFont font, float fontSize, float fieldWidth) {
        if (fieldWidth <= 0) {
            return 1; // Si no podemos calcular el ancho, asumimos 1 línea
        }

        try {
            float textWidth = font.getWidth(texto, fontSize);
            // Agregar un pequeño margen de seguridad (95% del ancho)
            int lineas = (int) Math.ceil(textWidth / (fieldWidth * 0.95f));
            return Math.max(1, lineas);
        } catch (Exception e) {
            log.warn("Error calculando líneas para texto: {}", texto, e);
            return 1;
        }
    }

    @Override
    public byte[] generateCompletePolicyPdf(Template01ApData template01Data,
                                            Template02ApData template02Data) {
        try (ByteArrayOutputStream resultStream = new ByteArrayOutputStream()) {

            log.info("🔄 Generando PDF completo de póliza (archivos adjuntos: {})...",
                    template02Data.getCreatedWithFiles());

            // 1️⃣ Generar Template01
            ByteArrayOutputStream template01Stream = new ByteArrayOutputStream();
            generateTemplate01Pdf(template01Data, template01Stream);
            byte[] template01Bytes = template01Stream.toByteArray();
            log.debug("✅ Template01 generado: {} bytes", template01Bytes.length);

            // 2️⃣ Lista para almacenar páginas del Template02
            List<byte[]> template02Pages = new ArrayList<>();

            // 🔀 DECISIÓN: ¿Tiene archivos adjuntos o lista de asegurados?
            if (Boolean.TRUE.equals(template02Data.getCreatedWithFiles())) {
                // 📎 MODO ARCHIVOS ADJUNTOS
                log.info("📎 Modo archivos adjuntos: procesando {} archivos...",
                        template02Data.getAttachedFiles() != null ? template02Data.getAttachedFiles().size() : 0);

                // Generar Template02 básico (sin tabla de asegurados)
                ByteArrayOutputStream template02BasicStream = new ByteArrayOutputStream();
                Template02ApData.PageData basicPage = Template02ApData.PageData.builder()
                        .tipo(template02Data.getTipo())
                        .numPoliza(template02Data.getNumPoliza())
                        .valor(template02Data.getValor())
                        .plan(template02Data.getPlan())
                        .createdWithFiles(true)
                        .pageNumber(1)
                        .totalPages(1)
                        .build();
                generateTemplate02Pdf(basicPage, template02BasicStream);
                byte[] template02BasicBytes = template02BasicStream.toByteArray();
                template02Pages.add(template02BasicBytes);
                log.debug("✅ Template02 básico generado: {} bytes", template02BasicBytes.length);

                // Procesar archivos adjuntos
                List<EmailAttachment> attachments = template02Data.getAttachedFiles();
                if (attachments != null && !attachments.isEmpty()) {
                    for (EmailAttachment attachment : attachments) {
                        byte[] fileData = attachment.getContent();
                        String contentType = attachment.getContentType();

                        try {
                            if ("application/pdf".equalsIgnoreCase(contentType)) {
                                // 📄 PDF válido - Normalizar al tamaño de template02
                                byte[] normalizedPdf = normalizeAttachmentToTemplateSize(
                                        fileData,
                                        contentType,
                                        template02BasicBytes
                                );
                                template02Pages.add(normalizedPdf);
                                log.info("✅ PDF normalizado y redimensionado: {}", attachment.getFilename());

                            } else if (contentType != null && contentType.startsWith("image/")) {
                                // 🖼️ Convertir imagen a PDF
                                byte[] pdfFromImage = convertImageToPdf(fileData, contentType, template02BasicBytes);
                                template02Pages.add(pdfFromImage);
                                log.info("✅ Imagen convertida a PDF: {}", attachment.getFilename());

                            } else {
                                log.warn("⚠️ Tipo de archivo no soportado: {} ({})",
                                        attachment.getFilename(), contentType);
                            }

                        } catch (Exception ex) {
                            log.warn("⚠️ No se pudo procesar adjunto {}: {}",
                                    attachment.getFilename(), ex.getMessage(), ex);
                        }
                    }
                }

            } else {
                // 👥 MODO LISTA DE ASEGURADOS (CON PAGINACIÓN)
                log.info("👥 Modo lista de asegurados: {} persona(s) para póliza {}",
                        template02Data.getNombres() != null ? template02Data.getNombres().size() : 0,
                        template02Data.getNumPoliza());

                PdfFont openSansFont = createOpenSansFont();
                float fieldWidth = 500f; // Ajusta según el ancho real de tu campo "nombres"
                List<Template02ApData.PageData> pages = templatePaginator.splitIntoPages(
                        template02Data,
                        openSansFont,
                        16f,
                        fieldWidth
                );

                if (pages.isEmpty()) {
                    log.warn("⚠️ No se generaron páginas con paginador. Creando página por defecto.");
                    // Página por defecto vacía
                    pages.add(Template02ApData.PageData.builder()
                            .tipo(template02Data.getTipo())
                            .numPoliza(template02Data.getNumPoliza())
                            .valor(template02Data.getValor())
                            .plan(template02Data.getPlan())
                            .createdWithFiles(false)
                            .pageNumber(1)
                            .totalPages(1)
                            .nombres(template02Data.getNombres())
                            .documentos(template02Data.getDocumentos())
                            .fechasDesde(template02Data.getFechasDesde())
                            .fechasHasta(template02Data.getFechasHasta())
                            .build());
                }

                log.info("📄 Generando {} página(s) de Template02 con asegurados...", pages.size());

                // Generar cada página
                for (Template02ApData.PageData pageData : pages) {
                    ByteArrayOutputStream pageStream = new ByteArrayOutputStream();
                    generateTemplate02Pdf(pageData, pageStream);
                    byte[] pageBytes = pageStream.toByteArray();
                    template02Pages.add(pageBytes);
                    log.debug("✅ Template02 página {}/{} generada: {} bytes",
                            pageData.getPageNumber(), pageData.getTotalPages(), pageBytes.length);
                }
            }

            // 3️⃣ Combinar todos los PDFs (Template01 + todas las páginas de Template02)
            try (PdfDocument resultPdf = new PdfDocument(new PdfWriter(resultStream));
                 PdfDocument template01Pdf = new PdfDocument(new PdfReader(new ByteArrayInputStream(template01Bytes)))) {

                PdfMerger merger = new PdfMerger(resultPdf);

                // Añadir Template01
                merger.merge(template01Pdf, 1, template01Pdf.getNumberOfPages());
                log.debug("✅ Template01 añadido al PDF final");

                // Añadir todas las páginas de Template02
                for (int i = 0; i < template02Pages.size(); i++) {
                    try (PdfDocument template02Pdf = new PdfDocument(
                            new PdfReader(new ByteArrayInputStream(template02Pages.get(i))))
                    ) {
                        merger.merge(template02Pdf, 1, template02Pdf.getNumberOfPages());
                        log.debug("✅ Template02 página {} añadida al PDF final", i + 1);
                    }
                }
            }

            byte[] finalBytes = resultStream.toByteArray();
            log.info("🎉 PDF completo generado exitosamente: {} bytes, {} página(s) Template02 (modo: {})",
                    finalBytes.length,
                    template02Pages.size(),
                    Boolean.TRUE.equals(template02Data.getCreatedWithFiles())
                            ? "archivos adjuntos" : "lista asegurados");

            return finalBytes;

        } catch (IOException e) {
            log.error("❌ Error generando PDF completo", e);
            throw new RuntimeException("Failed to generate complete policy PDF", e);
        }
    }

    /**
     * Obtiene el color según el tipo de póliza
     * @param tipo Tipo de póliza (1 o 2)
     * @param defaultColor Color por defecto si es tipo 1
     * @return Color RGB correspondiente
     */
    private DeviceRgb getColorByType(Long tipo, DeviceRgb defaultColor) {
        if (tipo != null && (tipo == 2L || tipo == 3L)) {
            return new DeviceRgb(65, 104, 255); // #4168FF para tipo 2 y 3
        }
        return defaultColor;
    }

    /**
     * Selecciona el resource correcto para template02 según si tiene archivos adjuntos
     */
    private Resource selectTemplate02ResourceWithFileSupport(Long tipo, Boolean createdWithFiles) {
        // Si la póliza fue creada con archivos, usar el template especial
        if (Boolean.TRUE.equals(createdWithFiles)) {
            log.debug("✅ Póliza con archivos adjuntos: Usando template02FilePath.pdf");
            return pdfConfig.getTemplate02FileResource();
        }

        // Si no, usar la lógica normal según tipo
        return selectTemplate02Resource(tipo);
    }

    /**
     * Verifica si un archivo es imagen o PDF
     */
    private boolean isImageOrPdf(String contentType, String fileName) {
        if (contentType == null && fileName == null) {
            return false;
        }

        // Verificar por contentType
        if (contentType != null) {
            if (contentType.startsWith("image/") ||
                    contentType.equals("application/pdf")) {
                return true;
            }
        }

        // Verificar por extensión de archivo
        if (fileName != null) {
            String lowerFileName = fileName.toLowerCase();
            return lowerFileName.endsWith(".jpg") ||
                    lowerFileName.endsWith(".jpeg") ||
                    lowerFileName.endsWith(".png") ||
                    lowerFileName.endsWith(".pdf") ||
                    lowerFileName.endsWith(".gif") ||
                    lowerFileName.endsWith(".bmp");
        }

        return false;
    }

    /**
     * Convierte un archivo imagen a PDF usando iText
     */
    private byte[] convertImageToPdf(byte[] imageData, String contentType, byte[] template02Bytes) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // 🧩 1. Leer el tamaño de página del Template02
            PdfDocument tempDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(template02Bytes)));
            Rectangle pageSize = tempDoc.getFirstPage().getPageSize();
            tempDoc.close();

            // 📄 2. Crear nuevo PDF con el mismo tamaño
            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(baos));
            com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdfDoc, new PageSize(pageSize));

            // 🖼️ 3. Crear la imagen desde bytes
            com.itextpdf.io.image.ImageData imageDataObj =
                    com.itextpdf.io.image.ImageDataFactory.create(imageData);
            com.itextpdf.layout.element.Image image =
                    new com.itextpdf.layout.element.Image(imageDataObj);

            // 📏 4. Calcular escala para llenar el tamaño de la plantilla sin deformar
            float pageWidth = pageSize.getWidth();
            float pageHeight = pageSize.getHeight();
            float imageWidth = image.getImageWidth();
            float imageHeight = image.getImageHeight();

            float widthScale = pageWidth / imageWidth;
            float heightScale = pageHeight / imageHeight;
            float scale = Math.min(widthScale, heightScale);

            image.scale(scale, scale);

            // 📍 5. Centrar la imagen
            float xOffset = (pageWidth - image.getImageScaledWidth()) / 2;
            float yOffset = (pageHeight - image.getImageScaledHeight()) / 2;
            image.setFixedPosition(xOffset, yOffset);

            document.add(image);
            document.close();

            return baos.toByteArray();
        }
    }

    /**
     * Normaliza los PDFs adjuntos al tamaño de la plantilla
     * <CHANGE> Ahora cierra correctamente el Document y maneja múltiples páginas
     */
    private byte[] normalizeAttachmentToTemplateSize(byte[] attachmentBytes, String contentType, byte[] template02Bytes) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // 1️⃣ Leer el tamaño de la primera página de template02
            PdfDocument templateDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(template02Bytes)));
            Rectangle templateSize = templateDoc.getFirstPage().getPageSize();
            templateDoc.close();

            // 2️⃣ Crear nuevo documento PDF con ese tamaño
            PdfDocument resultDoc = new PdfDocument(new PdfWriter(baos));

            if (contentType != null && contentType.toLowerCase().contains("pdf")) {
                // 🧩 Si el archivo es un PDF
                PdfDocument srcPdf = new PdfDocument(new PdfReader(new ByteArrayInputStream(attachmentBytes)));

                for (int i = 1; i <= srcPdf.getNumberOfPages(); i++) {
                    PdfPage srcPage = srcPdf.getPage(i);
                    Rectangle srcSize = srcPage.getPageSize();

                    // Crear una nueva página con el tamaño del template
                    PdfPage newPage = resultDoc.addNewPage(new PageSize(templateSize));
                    PdfCanvas canvas = new PdfCanvas(newPage);

                    // Calcular escala para ajustar proporción
                    float scaleX = templateSize.getWidth() / srcSize.getWidth();
                    float scaleY = templateSize.getHeight() / srcSize.getHeight();
                    float scale = Math.min(scaleX, scaleY);

                    // Centrar contenido escalado
                    float offsetX = (templateSize.getWidth() - (srcSize.getWidth() * scale)) / 2f;
                    float offsetY = (templateSize.getHeight() - (srcSize.getHeight() * scale)) / 2f;

                    // Importar contenido de la página original escalado y centrado
                    PdfFormXObject pageCopy = srcPage.copyAsFormXObject(resultDoc);
                    canvas.saveState();
                    canvas.concatMatrix(scale, 0, 0, scale, offsetX, offsetY);
                    canvas.addXObjectAt(pageCopy, 0, 0);
                    canvas.restoreState();

                    log.debug("✅ Página {} normalizada y redimensionada", i);
                }
                srcPdf.close();
            }

            resultDoc.close();
            return baos.toByteArray();
        }
    }
}