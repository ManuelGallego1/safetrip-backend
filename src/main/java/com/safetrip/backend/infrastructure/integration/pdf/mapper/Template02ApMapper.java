package com.safetrip.backend.infrastructure.integration.pdf.mapper;

import com.safetrip.backend.domain.model.*;
import com.safetrip.backend.infrastructure.integration.pdf.dto.Template02ApData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class Template02ApMapper {

    /**
     * Mapea los datos de la póliza y sus asegurados al DTO para generar el PDF Template02
     */
    public Template02ApData toTemplate02ApData(
            Policy policy,
            PolicyPayment policyPayment,
            List<PolicyPerson> policyPersons) {

        log.debug("Mapeando datos para Template02Ap - Póliza: {} con {} asegurados",
                policy.getPolicyId(), policyPersons.size());

        // Calcular valor total
        BigDecimal totalValue = policyPayment != null && policyPayment.getAppliedAmount() != null
                ? policyPayment.getAppliedAmount()
                : BigDecimal.ZERO;

        String formattedValue = formatMoney(totalValue);

        // Extraer nombres y documentos de todos los asegurados
        List<String> nombres = new ArrayList<>();
        List<String> documentos = new ArrayList<>();

        for (PolicyPerson pp : policyPersons) {
            Person person = pp.getPerson();

            // Agregar nombre
            nombres.add(person.getFullName());

            // Agregar documento (Tipo + Número)
            String documento = person.getDocumentType().getCode() + " " + person.getDocumentNumber();
            documentos.add(documento);
        }

        log.debug("✅ {} nombres y {} documentos extraídos", nombres.size(), documentos.size());

        return Template02ApData.builder()
                .numPoliza(policy.getPolicyNumber() != null
                        ? policy.getPolicyNumber()
                        : "PENDIENTE")
                .valor(formattedValue)
                .nombres(nombres)
                .documentos(documentos)
                .build();
    }

    /**
     * Divide los datos en páginas de máximo 20 asegurados cada una
     */
    public List<Template02ApData.PageData> splitIntoPages(Template02ApData data) {
        final int MAX_PER_PAGE = 20;

        List<String> allNombres = data.getNombres();
        List<String> allDocumentos = data.getDocumentos();

        int totalAsegurados = allNombres.size();
        int totalPages = (int) Math.ceil((double) totalAsegurados / MAX_PER_PAGE);

        log.debug("Dividiendo {} asegurados en {} página(s)", totalAsegurados, totalPages);

        List<Template02ApData.PageData> pages = new ArrayList<>();

        for (int pageNum = 0; pageNum < totalPages; pageNum++) {
            int fromIndex = pageNum * MAX_PER_PAGE;
            int toIndex = Math.min(fromIndex + MAX_PER_PAGE, totalAsegurados);

            List<String> pageNombres = allNombres.subList(fromIndex, toIndex);
            List<String> pageDocumentos = allDocumentos.subList(fromIndex, toIndex);

            Template02ApData.PageData page = Template02ApData.PageData.builder()
                    .numPoliza(data.getNumPoliza())
                    .valor(data.getValor())
                    .nombres(pageNombres)
                    .documentos(pageDocumentos)
                    .pageNumber(pageNum + 1)
                    .totalPages(totalPages)
                    .build();

            pages.add(page);

            log.debug("Página {}/{}: {} asegurados (índices {}-{})",
                    pageNum + 1, totalPages, pageNombres.size(), fromIndex, toIndex - 1);
        }

        return pages;
    }

    /**
     * Formatea un valor monetario con separadores de miles y sin decimales
     * Ejemplo: 150000 -> "150.000"
     */
    private String formatMoney(BigDecimal value) {
        if (value == null) {
            return "0";
        }

        long longValue = value.longValue();
        return String.format("%,d", longValue).replace(",", ".");
    }
}