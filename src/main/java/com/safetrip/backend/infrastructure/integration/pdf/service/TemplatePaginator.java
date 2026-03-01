package com.safetrip.backend.infrastructure.integration.pdf.service;

import com.itextpdf.kernel.font.PdfFont;
import com.safetrip.backend.infrastructure.integration.pdf.dto.Template02ApData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TemplatePaginator {

    private static final int MAX_LINES_PER_PAGE = 46;

    /**
     * Divide los datos en páginas basándose en el número REAL de líneas que ocupan los nombres
     * @param data Datos completos a paginar
     * @param font Fuente para calcular ancho de texto
     * @param fontSize Tamaño de fuente
     * @param fieldWidth Ancho del campo en el PDF
     * @return Lista de páginas con datos paginados
     */
    public List<Template02ApData.PageData> splitIntoPages(
            Template02ApData data,
            PdfFont font,
            float fontSize,
            float fieldWidth) {

        List<String> nombres = data.getNombres();
        List<String> documentos = data.getDocumentos();
        List<String> fechasDesde = data.getFechasDesde();
        List<String> fechasHasta = data.getFechasHasta();

        if (nombres == null || nombres.isEmpty()) {
            log.warn("⚠️ Lista de nombres vacía, retornando página vacía");
            return List.of(createEmptyPage(data));
        }

        // 1️⃣ Calcular cuántas líneas ocupa cada persona
        List<Integer> lineasPorPersona = new ArrayList<>();
        for (String nombre : nombres) {
            int lineas = calcularLineasNecesarias(nombre, font, fontSize, fieldWidth);
            lineasPorPersona.add(lineas);
        }

        // 2️⃣ Dividir en páginas según líneas reales
        List<Template02ApData.PageData> pages = new ArrayList<>();
        int personaIndex = 0;
        int lineasAcumuladas = 0;

        List<String> pageNames = new ArrayList<>();
        List<String> pageDocs = new ArrayList<>();
        List<String> pageFromDates = new ArrayList<>();
        List<String> pageToDates = new ArrayList<>();
        List<Integer> pageLines = new ArrayList<>();

        while (personaIndex < nombres.size()) {
            int lineasDeEstaPersona = lineasPorPersona.get(personaIndex);

            // Verificar si agregar esta persona excede el límite
            if (lineasAcumuladas + lineasDeEstaPersona > MAX_LINES_PER_PAGE && !pageNames.isEmpty()) {
                // Guardar página actual
                pages.add(createPage(
                        data,
                        pageNames,
                        pageDocs,
                        pageFromDates,
                        pageToDates,
                        pageLines,
                        pages.size() + 1
                ));

                // Resetear para nueva página
                pageNames = new ArrayList<>();
                pageDocs = new ArrayList<>();
                pageFromDates = new ArrayList<>();
                pageToDates = new ArrayList<>();
                pageLines = new ArrayList<>();
                lineasAcumuladas = 0;
            }

            // Agregar persona a la página actual
            pageNames.add(nombres.get(personaIndex));
            pageDocs.add(documentos.get(personaIndex));

            if (fechasDesde != null && personaIndex < fechasDesde.size()) {
                pageFromDates.add(fechasDesde.get(personaIndex));
            }
            if (fechasHasta != null && personaIndex < fechasHasta.size()) {
                pageToDates.add(fechasHasta.get(personaIndex));
            }

            pageLines.add(lineasDeEstaPersona);
            lineasAcumuladas += lineasDeEstaPersona;
            personaIndex++;
        }

        // 3️⃣ Agregar última página si tiene datos
        if (!pageNames.isEmpty()) {
            pages.add(createPage(
                    data,
                    pageNames,
                    pageDocs,
                    pageFromDates,
                    pageToDates,
                    pageLines,
                    pages.size() + 1
            ));
        }

        // 4️⃣ Actualizar totalPages en todas las páginas
        int totalPages = pages.size();
        for (Template02ApData.PageData page : pages) {
            page.setTotalPages(totalPages);
        }

        log.info("📊 Paginación completada: {} personas divididas en {} página(s)",
                nombres.size(), totalPages);

        return pages;
    }

    /**
     * Versión simplificada sin cálculo de líneas (usa el método anterior con valores por defecto)
     */
    public List<Template02ApData.PageData> splitIntoPages(Template02ApData data) {
        log.warn("⚠️ Usando paginación sin cálculo de líneas reales. Se asume 1 línea por persona.");
        return splitIntoPagesSimple(data);
    }

    /**
     * Método de respaldo que asume 1 línea por persona
     */
    private List<Template02ApData.PageData> splitIntoPagesSimple(Template02ApData data) {
        List<String> nombres = data.getNombres();
        List<String> documentos = data.getDocumentos();
        List<String> fechasDesde = data.getFechasDesde();
        List<String> fechasHasta = data.getFechasHasta();

        if (nombres == null || nombres.isEmpty()) {
            return List.of(createEmptyPage(data));
        }

        int totalPersonas = nombres.size();
        int totalPages = (int) Math.ceil((double) totalPersonas / MAX_LINES_PER_PAGE);

        List<Template02ApData.PageData> pages = new ArrayList<>();

        for (int i = 0; i < totalPages; i++) {
            int from = i * MAX_LINES_PER_PAGE;
            int to = Math.min(from + MAX_LINES_PER_PAGE, totalPersonas);

            List<String> pageNames = new ArrayList<>(nombres.subList(from, to));
            List<String> pageDocs = new ArrayList<>(documentos.subList(from, to));
            List<String> pageFromDates = fechasDesde != null ?
                    new ArrayList<>(fechasDesde.subList(from, to)) : new ArrayList<>();
            List<String> pageToDates = fechasHasta != null ?
                    new ArrayList<>(fechasHasta.subList(from, to)) : new ArrayList<>();

            pages.add(Template02ApData.PageData.builder()
                    .tipo(data.getTipo())
                    .numPoliza(data.getNumPoliza())
                    .valor(data.getValor())
                    .createdWithFiles(false)
                    .nombres(pageNames)
                    .documentos(pageDocs)
                    .fechasDesde(pageFromDates)
                    .fechasHasta(pageToDates)
                    .pageNumber(i + 1)
                    .totalPages(totalPages)
                    .build());

            log.debug("📄 Página {}/{} -> {} personas", i + 1, totalPages, pageNames.size());
        }

        return pages;
    }

    /**
     * Crea una página con los datos proporcionados
     */
    private Template02ApData.PageData createPage(
            Template02ApData data,
            List<String> nombres,
            List<String> documentos,
            List<String> fechasDesde,
            List<String> fechasHasta,
            List<Integer> lineasPorNombre,
            int pageNumber) {

        int totalLineas = lineasPorNombre.stream().mapToInt(Integer::intValue).sum();

        Template02ApData.PageData page = Template02ApData.PageData.builder()
                .tipo(data.getTipo())
                .numPoliza(data.getNumPoliza())
                .valor(data.getValor())
                .plan(data.getPlan())
                .createdWithFiles(false)
                .nombres(new ArrayList<>(nombres))
                .documentos(new ArrayList<>(documentos))
                .fechasDesde(fechasDesde.isEmpty() ? null : new ArrayList<>(fechasDesde))
                .fechasHasta(fechasHasta.isEmpty() ? null : new ArrayList<>(fechasHasta))
                .lineasPorNombre(new ArrayList<>(lineasPorNombre))
                .pageNumber(pageNumber)
                .totalPages(0) // Se actualizará después
                .build();

        log.debug("📄 Página {} creada: {} personas, {} líneas totales",
                pageNumber, nombres.size(), totalLineas);

        return page;
    }

    /**
     * Crea una página vacía
     */
    private Template02ApData.PageData createEmptyPage(Template02ApData data) {
        return Template02ApData.PageData.builder()
                .tipo(data.getTipo())
                .numPoliza(data.getNumPoliza())
                .plan(data.getPlan())
                .valor(data.getValor())
                .createdWithFiles(false)
                .nombres(new ArrayList<>())
                .documentos(new ArrayList<>())
                .fechasDesde(new ArrayList<>())
                .fechasHasta(new ArrayList<>())
                .lineasPorNombre(new ArrayList<>())
                .pageNumber(1)
                .totalPages(1)
                .build();
    }

    /**
     * Calcula cuántas líneas necesitará un texto dado el ancho del campo
     * Considera el word wrapping (saltos de palabra) correctamente
     */
    private int calcularLineasNecesarias(String texto, PdfFont font, float fontSize, float fieldWidth) {
        if (fieldWidth <= 0 || texto == null || texto.isEmpty()) {
            return 1;
        }

        try {
            float anchoDisponible = fieldWidth * 0.50f;

            // Dividir el texto en palabras
            String[] palabras = texto.split("\\s+");

            int lineas = 1;
            float anchoLineaActual = 0;

            for (int i = 0; i < palabras.length; i++) {
                String palabra = palabras[i];

                // Calcular ancho de la palabra
                float anchoPalabra = font.getWidth(palabra, fontSize);

                // Agregar espacio si no es la primera palabra de la línea
                float anchoConEspacio = anchoPalabra;
                if (anchoLineaActual > 0) {
                    anchoConEspacio += font.getWidth(" ", fontSize);
                }

                // Verificar si cabe en la línea actual
                if (anchoLineaActual + anchoConEspacio > anchoDisponible) {
                    // No cabe, necesitamos nueva línea
                    lineas++;
                    anchoLineaActual = anchoPalabra;
                } else {
                    // Cabe en la línea actual
                    anchoLineaActual += anchoConEspacio;
                }
            }

            int resultado = Math.max(1, lineas);

            if (resultado > 1) {
                log.debug("📏 '{}' ocupará {} líneas (ancho campo: {}, ancho texto: {})",
                        texto, resultado, fieldWidth, font.getWidth(texto, fontSize));
            }

            return resultado;

        } catch (Exception e) {
            log.warn("⚠️ Error calculando líneas para: '{}', asumiendo 1 línea", texto, e);
            return 1;
        }
    }
}