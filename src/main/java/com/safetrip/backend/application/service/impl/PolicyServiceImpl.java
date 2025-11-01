package com.safetrip.backend.application.service.impl;

import com.safetrip.backend.application.dto.PaymentDTO;
import com.safetrip.backend.application.service.FileAppService;
import com.safetrip.backend.application.service.PaymentService;
import com.safetrip.backend.application.service.PolicyService;
import com.safetrip.backend.application.validator.PersonValidator;
import com.safetrip.backend.domain.model.*;
import com.safetrip.backend.domain.model.enums.DocumentType;
import com.safetrip.backend.domain.model.enums.PolicyDataSource;
import com.safetrip.backend.domain.model.enums.RelationshipType;
import com.safetrip.backend.domain.repository.*;
import com.safetrip.backend.web.dto.mapper.PolicyResponseMapper;
import com.safetrip.backend.web.dto.request.CreatePolicyRequest;
import com.safetrip.backend.web.dto.request.PersonPolicyRequest;
import com.safetrip.backend.web.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;
    private final PolicyTypeRepository policyTypeRepository;
    private final PersonRepository personRepository;
    private final PolicyPersonRepository policyPersonRepository;
    private final PolicyDetailRepository policyDetailRepository;
    private final FileAppService fileAppService;
    private final PaymentService paymentService;
    private final PolicyResponseMapper policyResponseMapper;

    // Tipos de documento permitidos (solo personas naturales)
    private static final Set<DocumentType> ALLOWED_DOCUMENT_TYPES = Set.of(
            DocumentType.CC,
            DocumentType.PASSPORT,
            DocumentType.CE,
            DocumentType.NUIP
    );
    private final PolicyPaymentRepository policyPaymentRepository;

    @Override
    @Transactional
    public CreatePolicyResponse createPreliminaryPolicy(
            CreatePolicyRequest request,
            MultipartFile dataFile,
            MultipartFile[] attachments) throws IOException {

        log.info("🚀 Iniciando creación de póliza preliminar");

        // 1. Detectar fuente de datos
        PolicyDataSource dataSource = detectDataSource(request, dataFile);
        log.info("📊 Fuente de datos detectada: {}", dataSource.getDisplayName());

        // 2. Extraer y validar personas según la fuente
        List<PersonPolicyRequest> validatedPersons = extractAndValidatePersons(
                dataSource,
                request,
                dataFile
        );
        log.info("✅ {} personas validadas correctamente", validatedPersons.size());

        // 3. Crear póliza
        Policy savedPolicy = createPolicy(request, validatedPersons.size(), dataFile != null);

        // 4. Agregar personas a la póliza
        addPersonsToPolicy(savedPolicy.getPolicyId(), validatedPersons);

        // 5. Guardar archivos (si existen)
        saveFiles(savedPolicy.getPolicyId(), dataFile, attachments);

        // 6. Crear detalle de póliza
        createPolicyDetail(savedPolicy, request);

        // 7. Generar pago
        String paymentUrl = generatePayment(request, savedPolicy.getPolicyId(), validatedPersons);

        log.info("🎉 Póliza {} creada exitosamente - URL pago: {}",
                savedPolicy.getPolicyId(), paymentUrl);

        return new CreatePolicyResponse(paymentUrl, policyResponseMapper.toDto(savedPolicy));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<PolicyResponseWithDetails>> getAllPoliciesForUser(int page, int size) {
        log.info("🔍 Obteniendo pólizas del usuario");

        User user = getAuthenticatedUser();
        PageRequest pageable = PageRequest.of(page, size);
        Page<Policy> policyPage = policyRepository
                .findByCreatedByUserIdOrderByCreatedAtDesc(user.getUserId(), pageable);

        if (policyPage.isEmpty()) {
            log.info("📭 No se encontraron pólizas");
            return ApiResponse.success(
                    "No se encontraron pólizas",
                    Collections.emptyList(),
                    new Pagination(0, size, 0, 0)
            );
        }

        List<PolicyResponseWithDetails> responseList = policyPage.getContent().stream()
                .map(policy -> {
                    try {
                        PolicyDetail detail = policyDetailRepository
                                .findByPolicyId(policy)
                                .orElseThrow();

                        PolicyPayment payment = policyPaymentRepository
                                .findByPolicy(policy)
                                .orElseThrow();

                        return policyResponseMapper.toDtoWithDetails(policy, detail, payment);
                    } catch (Exception ex) {
                        log.error("❌ Error mapeando póliza {}: {}",
                                policy.getPolicyId(), ex.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Pagination pagination = new Pagination(
                policyPage.getNumber(),
                policyPage.getSize(),
                policyPage.getTotalElements(),
                policyPage.getTotalPages()
        );

        log.info("✅ {} pólizas encontradas", responseList.size());

        return ApiResponse.success("Pólizas obtenidas exitosamente", responseList, pagination);
    }

    @Override
    @Transactional
    public int patchPolicy(Long policyId, String policyNumber, BigDecimal unitPrice, Integer personCount) {
        return policyRepository.patchPolicy(
                policyId,
                policyNumber,
                ZonedDateTime.now(),
                unitPrice,
                personCount
        );
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Detecta la fuente de datos según lo que se recibe
     */
    private PolicyDataSource detectDataSource(CreatePolicyRequest request, MultipartFile dataFile) {
        boolean hasManualPersons = request.getPersons() != null && !request.getPersons().isEmpty();
        boolean hasDataFile = dataFile != null && !dataFile.isEmpty();

        // Validar que no se envíen ambos
        if (hasManualPersons && hasDataFile) {
            throw new IllegalArgumentException(
                    "No puede proporcionar personas manualmente y mediante archivo al mismo tiempo"
            );
        }

        // Validar que al menos uno esté presente
        if (!hasManualPersons && !hasDataFile) {
            throw new IllegalArgumentException(
                    "Debe proporcionar personas manualmente, mediante Excel o imagen"
            );
        }

        // Si hay archivo, detectar el tipo
        if (hasDataFile) {
            String filename = dataFile.getOriginalFilename();
            if (filename == null) {
                throw new IllegalArgumentException("El archivo no tiene nombre");
            }

            String lowerFilename = filename.toLowerCase();

            if (lowerFilename.endsWith(".xlsx") || lowerFilename.endsWith(".xls")) {
                return PolicyDataSource.EXCEL;
            } else if (lowerFilename.endsWith(".jpg") ||
                    lowerFilename.endsWith(".jpeg") ||
                    lowerFilename.endsWith(".png") ||
                    lowerFilename.endsWith(".pdf")) {
                return PolicyDataSource.IMAGE;
            } else {
                throw new IllegalArgumentException(
                        "Tipo de archivo no soportado. Use .xlsx/.xls para Excel o .jpg/.png/.pdf para imágenes"
                );
            }
        }

        // Si no hay archivo, es entrada manual
        return PolicyDataSource.MANUAL;
    }

    /**
     * Extrae y valida personas según la fuente de datos
     */
    private List<PersonPolicyRequest> extractAndValidatePersons(
            PolicyDataSource dataSource,
            CreatePolicyRequest request,
            MultipartFile dataFile) throws IOException {

        List<PersonPolicyRequest> persons;

        switch (dataSource) {
            case MANUAL:
                log.info("📝 Procesando entrada manual");
                persons = validateManualPersons(request.getPersons());
                break;

            case EXCEL:
                log.info("📊 Procesando archivo Excel");
                persons = parseAndValidateExcel(dataFile);
                break;

            case IMAGE:
                log.info("📷 Procesando imagen/PDF (OCR)");
                persons = parseAndValidateImage(dataFile);
                break;

            default:
                throw new IllegalStateException("Fuente de datos no soportada: " + dataSource);
        }

        // Validar que se hayan extraído personas
        if (persons == null || persons.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron personas válidas para asegurar");
        }

        log.info("✅ {} personas extraídas y validadas desde {}",
                persons.size(), dataSource.getDisplayName());

        return persons;
    }

    /**
     * Valida personas ingresadas manualmente
     */
    private List<PersonPolicyRequest> validateManualPersons(List<PersonPolicyRequest> persons) {
        if (persons == null || persons.isEmpty()) {
            throw new IllegalArgumentException("La lista de personas no puede estar vacía");
        }

        List<PersonPolicyRequest> validatedPersons = new ArrayList<>();
        Set<String> seenDocuments = new HashSet<>();
        int personIndex = 0;

        for (PersonPolicyRequest person : persons) {
            personIndex++;

            try {
                // Validar nombre completo
                String validatedName = PersonValidator.validateAndNormalizeFullName(
                        person.getFullName()
                );

                // Validar tipo de documento
                if (person.getDocumentType() == null) {
                    throw new IllegalArgumentException("El tipo de documento no puede estar vacío");
                }

                // Verificar que el tipo de documento esté permitido
                if (!ALLOWED_DOCUMENT_TYPES.contains(person.getDocumentType())) {
                    throw new IllegalArgumentException(
                            "Tipo de documento no permitido: " + person.getDocumentType().getCode() +
                                    ". Solo se permiten: CC, Pasaporte, CE, NUIP (personas naturales)"
                    );
                }

                // Validar número de documento
                String validatedDocNumber = PersonValidator.validateDocumentNumber(
                        person.getDocumentNumber()
                );

                // Verificar duplicados
                String documentKey = person.getDocumentType() + "-" + validatedDocNumber;
                if (seenDocuments.contains(documentKey)) {
                    throw new IllegalArgumentException(
                            "Documento duplicado: " + documentKey
                    );
                }
                seenDocuments.add(documentKey);

                validatedPersons.add(new PersonPolicyRequest(
                        validatedName,
                        person.getDocumentType(),
                        validatedDocNumber
                ));

                log.debug("✅ Persona {} validada: {} - {} {}",
                        personIndex, validatedName,
                        person.getDocumentType().getCode(), validatedDocNumber);

            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        String.format("Error validando persona #%d ('%s'): %s",
                                personIndex, person.getFullName(), e.getMessage())
                );
            }
        }

        return validatedPersons;
    }

    /**
     * Parsea y valida personas desde Excel
     */
    private List<PersonPolicyRequest> parseAndValidateExcel(MultipartFile excelFile) throws IOException {
        List<PersonPolicyRequest> list = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Set<String> seenDocuments = new HashSet<>();

        try (InputStream is = excelFile.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            boolean headerSkipped = false;
            int rowNumber = 0;

            for (Row row : sheet) {
                rowNumber++;

                // Saltar encabezado
                if (!headerSkipped) {
                    headerSkipped = true;
                    log.debug("Saltando encabezado en fila 1");
                    continue;
                }

                // Saltar filas vacías
                if (isRowEmpty(row)) {
                    log.debug("Saltando fila vacía: {}", rowNumber);
                    continue;
                }

                try {
                    // Columnas esperadas: A=Tipo Doc, B=Num Doc, C=Nombre
                    String docTypeStr = getCellString(row.getCell(0));
                    String docNumber = getCellString(row.getCell(1));
                    String fullName = getCellString(row.getCell(2));

                    // Validar que todos los campos estén presentes
                    if (docTypeStr == null || docTypeStr.trim().isEmpty()) {
                        errors.add("Fila " + rowNumber + ": Tipo de documento vacío");
                        continue;
                    }

                    if (docNumber == null || docNumber.trim().isEmpty()) {
                        errors.add("Fila " + rowNumber + ": Número de documento vacío");
                        continue;
                    }

                    if (fullName == null || fullName.trim().isEmpty()) {
                        errors.add("Fila " + rowNumber + ": Nombre completo vacío");
                        continue;
                    }

                    // Validar y normalizar
                    String validatedName = PersonValidator.validateAndNormalizeFullName(fullName);
                    DocumentType docType = DocumentType.fromValue(docTypeStr.trim());

                    // Verificar que el tipo de documento esté permitido
                    if (!ALLOWED_DOCUMENT_TYPES.contains(docType)) {
                        errors.add("Fila " + rowNumber + ": Tipo de documento no permitido '" +
                                docType.getCode() + "'. Solo se permiten: CC, Pasaporte, CE, NUIP");
                        continue;
                    }

                    String validatedDocNumber = PersonValidator.validateDocumentNumber(docNumber);

                    // Verificar duplicados
                    String documentKey = docType + "-" + validatedDocNumber;
                    if (seenDocuments.contains(documentKey)) {
                        errors.add("Fila " + rowNumber + ": Documento duplicado " + documentKey);
                        continue;
                    }
                    seenDocuments.add(documentKey);

                    list.add(new PersonPolicyRequest(validatedName, docType, validatedDocNumber));

                    log.debug("✅ Fila {} procesada: {} - {} {}",
                            rowNumber, validatedName, docType.getCode(), validatedDocNumber);

                } catch (IllegalArgumentException e) {
                    errors.add("Fila " + rowNumber + ": " + e.getMessage());
                } catch (Exception ex) {
                    errors.add("Fila " + rowNumber + ": Error inesperado - " + ex.getMessage());
                    log.error("❌ Error en fila {}: {}", rowNumber, ex.getMessage(), ex);
                }
            }
        }

        // Si hay errores, lanzar excepción con todos los detalles
        if (!errors.isEmpty()) {
            String errorMessage = "Errores encontrados en el Excel:\n" + String.join("\n", errors);
            log.error("❌ {}", errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        // Validar que se hayan procesado personas
        if (list.isEmpty()) {
            throw new IllegalArgumentException(
                    "El Excel no contiene personas válidas. " +
                            "Asegúrate de que el archivo tenga el formato correcto: " +
                            "Columna A=Tipo Documento, B=Número, C=Nombre Completo"
            );
        }

        log.info("✅ Excel procesado correctamente: {} personas", list.size());
        return list;
    }

    /**
     * Parsea y valida personas desde imagen (OCR)
     * TODO: Implementar lógica de OCR
     */
    private List<PersonPolicyRequest> parseAndValidateImage(MultipartFile imageFile) throws IOException {
        log.warn("⚠️ Procesamiento de imágenes/PDF con OCR aún no implementado");

        // TODO: Implementar OCR aquí
        // 1. Validar que sea imagen o PDF
        // 2. Extraer texto usando servicio OCR (Tesseract, Google Vision, AWS Textract, etc.)
        // 3. Parsear el texto extraído buscando patrones de documentos
        // 4. Validar y normalizar las personas extraídas
        // 5. Retornar lista validada

        throw new UnsupportedOperationException(
                "El procesamiento de imágenes y PDFs con OCR estará disponible próximamente"
        );
    }

    /**
     * Crea la entidad Policy
     */
    private Policy createPolicy(CreatePolicyRequest request, int personCount, boolean hasFile) {
        PolicyType policyType = policyTypeRepository.findById(request.getPolicyTypeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tipo de póliza no encontrado: " + request.getPolicyTypeId()
                ));

        User createdBy = getAuthenticatedUser();

        Policy policy = new Policy(
                null,
                policyType,
                personCount,
                BigDecimal.ZERO,
                null,
                null,
                createdBy,
                ZonedDateTime.now(),
                ZonedDateTime.now(),
                hasFile,
                null,
                null
        );

        Policy savedPolicy = policyRepository.save(policy);
        log.info("✅ Póliza {} creada con {} personas", savedPolicy.getPolicyId(), personCount);

        return savedPolicy;
    }

    /**
     * Agrega personas a la póliza
     */
    private void addPersonsToPolicy(Long policyId, List<PersonPolicyRequest> persons) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Póliza no encontrada: " + policyId));

        User currentUser = policy.getCreatedByUser();

        for (PersonPolicyRequest pr : persons) {
            // Buscar o crear persona
            Person person = personRepository
                    .findByDocument(pr.getDocumentType(), pr.getDocumentNumber())
                    .orElseGet(() -> {
                        Person newPerson = new Person(
                                null,
                                pr.getFullName(),
                                pr.getDocumentType(),
                                pr.getDocumentNumber(),
                                null,
                                ZonedDateTime.now(),
                                ZonedDateTime.now()
                        );
                        Person saved = personRepository.save(newPerson);
                        log.debug("✅ Nueva persona creada: {} - {} {}",
                                saved.getFullName(),
                                saved.getDocumentType().getCode(),
                                saved.getDocumentNumber());
                        return saved;
                    });

            // Determinar tipo de relación
            RelationshipType relationshipType = Objects.equals(
                    person.getPersonId(),
                    currentUser.getUserId()
            ) ? RelationshipType.HOLDER : RelationshipType.BENEFICIARY;

            // Crear relación persona-póliza
            PolicyPerson pp = new PolicyPerson(
                    null,
                    policy,
                    person,
                    relationshipType,
                    ZonedDateTime.now(),
                    ZonedDateTime.now()
            );

            policyPersonRepository.save(pp);

            log.debug("✅ Persona agregada a póliza: {} como {}",
                    person.getFullName(), relationshipType);
        }

        log.info("✅ {} personas agregadas exitosamente a póliza {}", persons.size(), policyId);
    }

    /**
     * Guarda archivos relacionados con la póliza
     */
    private void saveFiles(Long policyId, MultipartFile dataFile, MultipartFile[] attachments)
            throws IOException {

        int filesCount = 0;

        if (dataFile != null && !dataFile.isEmpty()) {
            fileAppService.uploadFileForPolicy(policyId, dataFile);
            filesCount++;
            log.info("✅ Archivo de datos guardado: {}", dataFile.getOriginalFilename());
        }

        if (attachments != null && attachments.length > 0) {
            for (MultipartFile attachment : attachments) {
                if (!attachment.isEmpty()) {
                    fileAppService.uploadFileForPolicy(policyId, attachment);
                    filesCount++;
                    log.debug("✅ Adjunto guardado: {}", attachment.getOriginalFilename());
                }
            }
        }

        if (filesCount > 0) {
            log.info("✅ Total de {} archivo(s) guardado(s)", filesCount);
        }
    }

    /**
     * Crea el detalle de la póliza
     */
    private void createPolicyDetail(Policy policy, CreatePolicyRequest request) {
        PolicyDetail detail = new PolicyDetail(
                null,
                policy,
                request.getOrigin(),
                request.getDestination(),
                request.getDeparture(),
                request.getArrival(),
                null,
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );

        policyDetailRepository.save(detail);
        log.info("✅ Detalle de póliza creado: {} -> {} ({} a {})",
                request.getOrigin(),
                request.getDestination(),
                request.getDeparture(),
                request.getArrival());
    }

    /**
     * Genera el pago para la póliza
     */
    private String generatePayment(
            CreatePolicyRequest request,
            Long policyId,
            List<PersonPolicyRequest> persons) {

        PaymentDTO paymentDTO = new PaymentDTO(
                request.getPolicyTypeId(),
                request.getPaymentTypeId(),
                policyId,
                request.getDeparture(),
                request.getArrival(),
                persons
        );

        String url;
        if (paymentDTO.getPaymentTypeId() == 1) {
            url = paymentService.createdPaymentWithZurich(paymentDTO);
            log.info("✅ Pago creado con Zurich para póliza {}", policyId);
        } else if (paymentDTO.getPaymentTypeId() == 2) {
            url = paymentService.cretaedPaymentWithWallet(paymentDTO);
            log.info("✅ Pago creado con Wallet para póliza {}", policyId);
        } else {
            throw new IllegalArgumentException(
                    "Tipo de pago no soportado: " + paymentDTO.getPaymentTypeId()
            );
        }

        return url;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadPoliciesConsolidatedExcel() {
        log.info("📊 Generando consolidado de pólizas en Excel");

        User user = getAuthenticatedUser();

        // Obtener todas las pólizas del usuario sin paginación
        List<Policy> policies = policyRepository.findByCreatedByUserIdOrderByCreatedAtDesc(user.getUserId());

        if (policies.isEmpty()) {
            log.warn("⚠️ No hay pólizas para exportar");
            throw new IllegalArgumentException("No tiene pólizas registradas para exportar");
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Consolidado de Pólizas");

            // Estilos
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle moneyStyle = createMoneyStyle(workbook);

            // Crear encabezado
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "Número Póliza",
                    "Tipo de Póliza",
                    "Cantidad Personas",
                    "Precio Unitario",
                    "Precio Total",
                    "Origen",
                    "Destino",
                    "Fecha Salida",
                    "Fecha Llegada",
                    "Fecha Creación",
                    "Creado con Archivo"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Llenar datos
            int rowNum = 1;
            for (Policy policy : policies) {
                try {
                    Row row = sheet.createRow(rowNum++);

                    // Obtener detalles y pago de la póliza
                    PolicyDetail detail = policyDetailRepository
                            .findByPolicyId(policy)
                            .orElse(null);

                    PolicyPayment payment = policyPaymentRepository
                            .findByPolicy(policy)
                            .orElse(null);


                    // Número Póliza
                    createCell(row, 0,
                            policy.getPolicyNumber() != null ? policy.getPolicyNumber() : "PENDIENTE",
                            dataStyle);

                    // Tipo de Póliza
                    String policyTypeName = policy.getPolicyType() != null
                            ? policy.getPolicyType().getName()
                            : "N/A";
                    createCell(row, 1, policyTypeName, dataStyle);

                    // Cantidad Personas
                    createCell(row, 2, String.valueOf(policy.getPersonCount()), dataStyle);

                    // Precio Total
                    BigDecimal totalPrice = payment != null && payment.getAppliedAmount() != null
                            ? payment.getAppliedAmount(): BigDecimal.ZERO;
                    Cell totalPriceCell = row.createCell(3);
                    totalPriceCell.setCellValue(totalPrice.doubleValue());
                    totalPriceCell.setCellStyle(moneyStyle);

                    // Origen
                    createCell(row, 4, detail != null ? detail.getOrigin() : "N/A", dataStyle);

                    // Destino
                    createCell(row, 5, detail != null ? detail.getDestination() : "N/A", dataStyle);

                    // Fecha Salida
                    if (detail != null && detail.getDeparture() != null) {
                        Cell dateCell = row.createCell(6);
                        dateCell.setCellValue(java.util.Date.from(detail.getDeparture().toInstant()));
                        dateCell.setCellStyle(dateStyle);
                    } else {
                        createCell(row, 6, "N/A", dataStyle);
                    }

                    // Fecha Llegada
                    if (detail != null && detail.getArrival() != null) {
                        Cell dateCell = row.createCell(7);
                        dateCell.setCellValue(java.util.Date.from(detail.getArrival().toInstant()));
                        dateCell.setCellStyle(dateStyle);
                    } else {
                        createCell(row, 7, "N/A", dataStyle);
                    }

                    // Fecha Creación
                    if (policy.getCreatedAt() != null) {
                        Cell dateCell = row.createCell(8);
                        dateCell.setCellValue(java.util.Date.from(policy.getCreatedAt().toInstant()));
                        dateCell.setCellStyle(dateStyle);
                    } else {
                        createCell(row, 8, "N/A", dataStyle);
                    }

                    // Creado con Archivo
                    createCell(row, 9,
                            policy.getCreatedWithFile() != null && policy.getCreatedWithFile() ? "SÍ" : "NO",
                            dataStyle);

                } catch (Exception ex) {
                    log.error("❌ Error procesando póliza {}: {}",
                            policy.getPolicyId(), ex.getMessage());
                }
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // Agregar un poco de padding extra
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 500);
            }

            // Convertir a bytes
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);

            log.info("✅ Excel generado exitosamente con {} pólizas", policies.size());

            return outputStream.toByteArray();

        } catch (IOException ex) {
            log.error("❌ Error generando Excel: {}", ex.getMessage(), ex);
            throw new RuntimeException("Error al generar el archivo Excel", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<InsuredPersonResponse>> getInsuredPersonsByPolicy(Long policyId) {
        log.info("🔍 Obteniendo asegurados de la póliza: {}", policyId);

        // Verificar que la póliza existe
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> {
                    log.error("❌ Póliza no encontrada: {}", policyId);
                    return new IllegalArgumentException("Póliza no encontrada con ID: " + policyId);
                });

        // Verificar que el usuario autenticado es el dueño de la póliza
        User currentUser = getAuthenticatedUser();
        if (!policy.getCreatedByUser().getUserId().equals(currentUser.getUserId())) {
            log.error("❌ Usuario no autorizado para ver los asegurados de la póliza: {}", policyId);
            throw new SecurityException("No tiene permisos para ver los asegurados de esta póliza");
        }

        // Obtener las relaciones póliza-persona
        List<PolicyPerson> policyPersons = policyPersonRepository.findByPolicyId(policyId);

        if (policyPersons.isEmpty()) {
            log.info("📭 No se encontraron asegurados para la póliza: {}", policyId);
            return ApiResponse.success(
                    "No se encontraron asegurados para esta póliza",
                    List.of()
            );
        }

        // Mapear a DTOs
        List<InsuredPersonResponse> insuredPersons = policyPersons.stream()
                .map(pp -> {
                    Person person = pp.getPerson();
                    return new InsuredPersonResponse(
                            person.getPersonId(),
                            person.getFullName(),
                            person.getDocumentType(),
                            person.getDocumentNumber(),
                            pp.getRelationship(),
                            pp.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());

        log.info("✅ {} asegurados encontrados para la póliza {}", insuredPersons.size(), policyId);

        return ApiResponse.success(
                String.format("Se encontraron %d asegurados", insuredPersons.size()),
                insuredPersons
        );
    }

    /**
     * Obtiene el usuario autenticado
     */
    private User getAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /**
     * Obtiene el valor string de una celda de Excel
     */
    private static String getCellString(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                // Convertir números a string sin decimales
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case BLANK:
                return null;
            default:
                return null;
        }
    }

    /**
     * Verifica si una fila de Excel está completamente vacía
     */
    private static boolean isRowEmpty(Row row) {
        if (row == null) return true;

        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellString(cell);
                if (value != null && !value.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy HH:mm"));
        return style;
    }

    private CellStyle createMoneyStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("$#,##0.00"));
        return style;
    }

    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }
}