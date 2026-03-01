package com.safetrip.backend.application.service.impl;

import com.safetrip.backend.application.dto.PaymentDTO;
import com.safetrip.backend.application.service.DiscountService;
import com.safetrip.backend.application.service.FileAppService;
import com.safetrip.backend.application.service.PaymentService;
import com.safetrip.backend.application.service.PolicyService;
import com.safetrip.backend.application.validator.PersonValidator;
import com.safetrip.backend.domain.model.*;
import com.safetrip.backend.domain.model.enums.DocumentType;
import com.safetrip.backend.domain.model.enums.PaymentStatus;
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
import org.springframework.transaction.annotation.Propagation;
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
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentTypeRepository paymentTypeRepository;

    // Tipos de documento permitidos (solo personas naturales)
    private static final Set<DocumentType> ALLOWED_DOCUMENT_TYPES = Set.of(
            DocumentType.CC,
            DocumentType.PASSPORT,
            DocumentType.CE,
            DocumentType.NUIP
    );
    private final PolicyPaymentRepository policyPaymentRepository;
    private final DiscountService discountService;

    /**
     * 🔥 MÉTODO PRINCIPAL SIN @Transactional
     * Delega el guardado a un método con transacción independiente
     */
    @Override
    public CreatePolicyResponse createPreliminaryPolicy(
            CreatePolicyRequest request,
            MultipartFile dataFile,
            MultipartFile[] attachments) throws IOException {

        log.info("🚀 Iniciando creación de póliza preliminar");

        MultipartFile primaryDataFile = dataFile;
        MultipartFile[] otherAttachments = attachments;

        if ((primaryDataFile == null || primaryDataFile.isEmpty()) &&
                attachments != null && attachments.length > 0) {
            primaryDataFile = attachments[0];
            if (attachments.length > 1) {
                otherAttachments = new MultipartFile[attachments.length - 1];
                System.arraycopy(attachments, 1, otherAttachments, 0, attachments.length - 1);
            } else {
                otherAttachments = new MultipartFile[0];
            }
            log.info("📎 Using first attachment as primary data file: {}", primaryDataFile.getOriginalFilename());
        }

        // 1. Detectar fuente de datos
        PolicyDataSource dataSource = detectDataSource(request, primaryDataFile);
        log.info("📊 Fuente de datos detectada: {}", dataSource.getDisplayName());

        // 2. Extraer y validar personas según la fuente
        List<PersonPolicyRequest> validatedPersons = extractAndValidatePersons(
                dataSource,
                request,
                primaryDataFile
        );
        log.info("✅ {} personas validadas correctamente", validatedPersons.size());

        // 3. Determinar el person count real
        int actualPersonCount = dataSource == PolicyDataSource.IMAGE
                ? request.getPersonCount()
                : validatedPersons.size();

        if (request.getPaymentTypeId() == 2 || request.getPaymentTypeId() == 3) {
            return processWalletPayment(
                    request,
                    actualPersonCount,
                    primaryDataFile,
                    otherAttachments,
                    validatedPersons,
                    dataSource
            );
        } else {
            // Pago con Zurich (flujo original)
            return processZurichPayment(
                    request,
                    actualPersonCount,
                    primaryDataFile,
                    otherAttachments,
                    validatedPersons,
                    dataSource
            );
        }
    }

    private CreatePolicyResponse processWalletPayment(
            CreatePolicyRequest request,
            int actualPersonCount,
            MultipartFile primaryDataFile,
            MultipartFile[] otherAttachments,
            List<PersonPolicyRequest> validatedPersons,
            PolicyDataSource dataSource) throws IOException {

        log.info("💼 Procesando pago con WALLET - Payment Type: {}", request.getPaymentTypeId());

        User currentUser = getAuthenticatedUser();

        // 1. Determinar el walletTypeId según paymentTypeId
        Long walletTypeId = determineWalletTypeId(request.getPaymentTypeId(), request.getPolicyTypeId());

        // 2. Buscar TODAS las wallets disponibles del tipo requerido
        List<Wallet> availableWallets = findAllAvailableWalletsByType(
                currentUser.getUserId(),
                walletTypeId,
                request.getPolicyTypeId()
        );

        // 3. Calcular monto requerido
        BigDecimal requiredAmount = calculateRequiredAmountForWalletType(
                walletTypeId,
                request,
                actualPersonCount
        );

        // 4. Verificar que el saldo total sea suficiente
        BigDecimal totalAvailable = availableWallets.stream()
                .map(Wallet::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAvailable.compareTo(requiredAmount) < 0) {
            throw new IllegalArgumentException(
                    String.format("Saldo insuficiente. Disponible: %s, Requerido: %s",
                            totalAvailable, requiredAmount)
            );
        }

        // 5. Seleccionar las wallets necesarias para cubrir el monto
        List<Wallet> walletsToUse = selectWalletsForPayment(availableWallets, requiredAmount);

        // 6. Generar número de póliza (usar la primera wallet con transactionId)
        Wallet primaryWallet = walletsToUse.get(0);
        String policyNumber = generatePolicyNumber(primaryWallet);

        // 7. Crear la póliza usando múltiples wallets
        Policy savedPolicy = savePolicyWithMultipleWallets(
                request,
                actualPersonCount,
                primaryDataFile,
                otherAttachments,
                validatedPersons,
                dataSource,
                policyNumber,
                walletsToUse,
                requiredAmount
        );

        log.info("✅ Póliza {} creada usando {} wallet(s)",
                savedPolicy.getPolicyNumber(), walletsToUse.size());

        return new CreatePolicyResponse(
                null,
                policyResponseMapper.toDto(savedPolicy)
        );
    }

    /**
     * Calcula el monto requerido según el tipo de wallet
     */
    private BigDecimal calculateRequiredAmountForWalletType(
            Long walletTypeId,
            CreatePolicyRequest request,
            int actualPersonCount) {

        long travelDays = java.time.temporal.ChronoUnit.DAYS.between(
                request.getDeparture().toLocalDate(),
                request.getArrival().toLocalDate()
        ) + 1;

        BigDecimal requiredAmount;
        Long policyTypeId = request.getPolicyTypeId();

        if (walletTypeId == 1L) {
            // PAX: personas × días
            requiredAmount = BigDecimal.valueOf(actualPersonCount * travelDays);
            log.info("🧮 PAX requeridos: {} × {} = {}",
                    actualPersonCount, travelDays, requiredAmount);

        } else if (walletTypeId == 2L) {
            // SUBSCRIPTION: solo habitaciones
            requiredAmount = BigDecimal.valueOf(actualPersonCount);
            log.info("🏨 Habitaciones requeridas: {}", requiredAmount);

        } else if (walletTypeId == 3L) {
            // CASH: calcular según tipo de póliza
            PolicyType policyType = policyTypeRepository.findById(policyTypeId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Tipo de póliza no encontrado: " + policyTypeId
                    ));

            if (policyTypeId == 1L || policyTypeId == 4L) {
                // AP o PAX: días
                requiredAmount = policyType.getBaseValue()
                        .multiply(BigDecimal.valueOf(travelDays))
                        .multiply(BigDecimal.valueOf(actualPersonCount));
            } else if (policyTypeId == 2L) {
                // SH: noches
                long nights = travelDays - 1;
                if (nights < 1) {
                    throw new IllegalArgumentException(
                            "El viaje debe ser de al menos 1 noche"
                    );
                }
                requiredAmount = policyType.getBaseValue()
                        .multiply(BigDecimal.valueOf(nights))
                        .multiply(BigDecimal.valueOf(actualPersonCount));
            } else {
                throw new IllegalArgumentException(
                        "Tipo de póliza no soportado: " + policyTypeId
                );
            }

            log.info("💵 Monto CASH calculado: ${}", requiredAmount);

        } else {
            throw new IllegalArgumentException(
                    "Tipo de wallet no soportado: " + walletTypeId
            );
        }

        return requiredAmount;
    }

    /**
     * Selecciona las wallets necesarias para cubrir el monto (FIFO)
     */
    private List<Wallet> selectWalletsForPayment(
            List<Wallet> availableWallets,
            BigDecimal requiredAmount) {

        List<Wallet> selectedWallets = new ArrayList<>();
        BigDecimal remaining = requiredAmount;

        log.info("💰 Seleccionando wallets para cubrir: {}", requiredAmount);

        for (Wallet wallet : availableWallets) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break; // Ya cubrimos el monto
            }

            selectedWallets.add(wallet);
            remaining = remaining.subtract(wallet.getTotal());

            log.info("  ➤ Wallet {} seleccionada: saldo {} (restante por cubrir: {})",
                    wallet.getWalletId(),
                    wallet.getTotal(),
                    remaining.max(BigDecimal.ZERO));
        }

        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException(
                    "Error: No se pudo cubrir el monto completo. Faltante: " + remaining
            );
        }

        log.info("✅ {} wallet(s) seleccionada(s) para el pago", selectedWallets.size());

        return selectedWallets;
    }

    private Wallet findAvailableWalletByPaymentType(Long userId, Long policyTypeId, Long paymentTypeId) {
        log.info("🔍 Buscando wallet - User: {}, PolicyType: {}, PaymentType: {}",
                userId, policyTypeId, paymentTypeId);

        // PaymentTypeId 2 = Wallet PAX/SUBSCRIPTION (según policyTypeId)
        // PaymentTypeId 3 = Wallet CASH (siempre walletTypeId = 3)

        Long walletTypeId;

        if (paymentTypeId == 2L) {
            // Determinar si es PAX o SUBSCRIPTION según el tipo de póliza
            if (policyTypeId == 4L) {
                // Póliza tipo PAX → Wallet PAX (tipo 1)
                walletTypeId = 1L;
            } else if (policyTypeId == 2L) {
                // Póliza tipo HOTEL → Wallet SUBSCRIPTION (tipo 2)
                walletTypeId = 2L;
            } else {
                throw new IllegalArgumentException(
                        "El tipo de póliza " + policyTypeId + " no es compatible con Payment Type 2 (Wallet PAX/SUBSCRIPTION)"
                );
            }
        } else if (paymentTypeId == 3L) {
            // Wallet CASH (tipo 3) - compatible con todos los tipos de póliza
            walletTypeId = 3L;
        } else {
            throw new IllegalArgumentException("Tipo de pago no soportado: " + paymentTypeId);
        }

        List<Wallet> wallets = walletRepository.findByUserAndWalletType(userId, walletTypeId);

        if (wallets.isEmpty()) {
            throw new IllegalArgumentException(
                    "No tienes una wallet activa del tipo requerido para esta póliza"
            );
        }

        // Filtrar solo wallets con transactionId válido
        List<Wallet> walletsWithTransactionId = wallets.stream()
                .filter(w -> w.getTransactionId() != null && !w.getTransactionId().trim().isEmpty())
                .collect(Collectors.toList());

        if (walletsWithTransactionId.isEmpty()) {
            throw new IllegalArgumentException(
                    "No tienes una wallet válida con Transaction ID para realizar pagos. " +
                            "Por favor, contacta a soporte o recarga tu billetera."
            );
        }

        // Para wallet tipo SUBSCRIPTION (ID 2)
        if (walletTypeId == 2L) {
            return findValidSubscriptionWallet(walletsWithTransactionId);
        }

        // Para wallet tipo CASH (ID 3)
        if (walletTypeId == 3L) {
            return findValidCashWallet(walletsWithTransactionId);
        }

        // Para wallet tipo PAX (ID 1)
        return findValidPaxWallet(walletsWithTransactionId);
    }

    /**
     * Determina el walletTypeId según el paymentTypeId y policyTypeId
     */
    private Long determineWalletTypeId(Long paymentTypeId, Long policyTypeId) {
        if (paymentTypeId == 2L) {
            // PAX o SUBSCRIPTION según policyTypeId
            if (policyTypeId == 4L) {
                return 1L; // PAX
            } else if (policyTypeId == 2L) {
                return 2L; // SUBSCRIPTION
            } else {
                throw new IllegalArgumentException(
                        "El tipo de póliza " + policyTypeId +
                                " no es compatible con Payment Type 2"
                );
            }
        } else if (paymentTypeId == 3L) {
            return 3L; // CASH
        } else {
            throw new IllegalArgumentException(
                    "Tipo de pago no soportado: " + paymentTypeId
            );
        }
    }

    /**
     * Busca TODAS las wallets disponibles del tipo especificado
     */
    private List<Wallet> findAllAvailableWalletsByType(
            Long userId,
            Long walletTypeId,
            Long policyTypeId) {

        log.info("🔍 Buscando todas las wallets - User: {}, WalletType: {}",
                userId, walletTypeId);

        List<Wallet> wallets = walletRepository.findByUserAndWalletType(userId, walletTypeId);

        if (wallets.isEmpty()) {
            throw new IllegalArgumentException(
                    "No tienes wallets activas del tipo requerido"
            );
        }

        // Filtrar wallets válidas con transactionId
        List<Wallet> validWallets = wallets.stream()
                .filter(w -> w.getTransactionId() != null && !w.getTransactionId().trim().isEmpty())
                .filter(w -> w.getTotal().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        if (validWallets.isEmpty()) {
            throw new IllegalArgumentException(
                    "No tienes wallets válidas con saldo disponible"
            );
        }

        // Validaciones específicas por tipo
        if (walletTypeId == 2L) {
            // SUBSCRIPTION: validar vigencia
            ZonedDateTime now = ZonedDateTime.now();
            validWallets = validWallets.stream()
                    .filter(w -> w.getStartDate() != null && w.getEndDate() != null)
                    .filter(w -> !now.isBefore(w.getStartDate()) && !now.isAfter(w.getEndDate()))
                    .collect(Collectors.toList());

            if (validWallets.isEmpty()) {
                throw new IllegalArgumentException(
                        "No tienes una suscripción hotelera activa y vigente"
                );
            }
        }

        // Ordenar por fecha de creación (más antiguas primero - FIFO)
        validWallets.sort(Comparator.comparing(Wallet::getCreatedAt));

        log.info("✅ {} wallet(s) válida(s) encontrada(s)", validWallets.size());

        return validWallets;
    }

    private Wallet findValidSubscriptionWallet(List<Wallet> wallets) {
        ZonedDateTime now = ZonedDateTime.now();

        Wallet validWallet = wallets.stream()
                .filter(w -> w.getStartDate() != null && w.getEndDate() != null)
                .filter(w -> !now.isBefore(w.getStartDate()) && !now.isAfter(w.getEndDate()))
                .filter(w -> w.getTotal().compareTo(BigDecimal.ZERO) > 0)
                .max(Comparator.comparing(Wallet::getEndDate))
                .orElseThrow(() -> new IllegalArgumentException(
                        "No tienes una suscripción hotelera activa y vigente. " +
                                "Verifica las fechas de vigencia o contrata un nuevo plan."
                ));

        log.info("✅ Wallet Hotelera vigente encontrada: {} (Transaction ID: {})",
                validWallet.getWalletId(), validWallet.getTransactionId());

        return validWallet;
    }

    private BigDecimal calculateRequiredAmount(
            Wallet wallet,
            CreatePolicyRequest request,
            int actualPersonCount) {

        // Calcular días de viaje (diferencia entre fechas + 1)
        long travelDays = java.time.temporal.ChronoUnit.DAYS.between(
                request.getDeparture().toLocalDate(),
                request.getArrival().toLocalDate()
        ) + 1;

        log.info("📅 Días de viaje: {}", travelDays);

        BigDecimal requiredAmount;
        Long walletTypeId = wallet.getWalletType().getWalletTypeId();
        Long policyTypeId = request.getPolicyTypeId();

        if (walletTypeId == 1L) {
            // ============================================
            // Wallet tipo PAX (para pólizas tipo PAX - policyTypeId 4)
            // ============================================
            // Cobro por DÍAS: personas * días
            requiredAmount = BigDecimal.valueOf(actualPersonCount * travelDays);
            log.info("🧮 PAX requeridos (personas × días): {} × {} = {}",
                    actualPersonCount, travelDays, requiredAmount);

        } else if (walletTypeId == 2L) {
            // ============================================
            // Wallet tipo SUBSCRIPTION (solo para SH - policyTypeId 2)
            // ============================================
            // Cobro por HABITACIONES (personas), sin importar las noches
            requiredAmount = BigDecimal.valueOf(actualPersonCount);
            log.info("🏨 Habitaciones requeridas: {}", requiredAmount);

        } else if (walletTypeId == 3L) {
            // ============================================
            // Wallet tipo CASH (para AP, SH y PAX)
            // ============================================
            PolicyType policyTypeEntity = policyTypeRepository.findById(policyTypeId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Tipo de póliza no encontrado: " + policyTypeId
                    ));

            if (policyTypeId == 1L) {
                // AP (Accidentes Personales): Cobro por DÍAS
                requiredAmount = policyTypeEntity.getBaseValue()
                        .multiply(BigDecimal.valueOf(travelDays))
                        .multiply(BigDecimal.valueOf(actualPersonCount));

                log.info("💵 Monto AP (precio × días × personas): ${} × {} × {} = ${}",
                        policyTypeEntity.getBaseValue(), travelDays, actualPersonCount, requiredAmount);

            } else if (policyTypeId == 2L) {
                // SH (Seguro Hotelero): Cobro por NOCHES
                long nights = travelDays - 1;

                if (nights < 1) {
                    throw new IllegalArgumentException(
                            "El viaje debe ser de al menos 1 noche (2 días). Días calculados: " + travelDays
                    );
                }

                requiredAmount = policyTypeEntity.getBaseValue()
                        .multiply(BigDecimal.valueOf(nights))
                        .multiply(BigDecimal.valueOf(actualPersonCount));

                log.info("💵 Monto SH (precio × noches × personas): ${} × {} noches × {} = ${}",
                        policyTypeEntity.getBaseValue(), nights, actualPersonCount, requiredAmount);

            } else if (policyTypeId == 4L) {
                // PAX con CASH: Cobro por DÍAS (igual que AP)
                requiredAmount = policyTypeEntity.getBaseValue()
                        .multiply(BigDecimal.valueOf(travelDays))
                        .multiply(BigDecimal.valueOf(actualPersonCount));

                log.info("💵 Monto PAX (precio × días × personas): ${} × {} × {} = ${}",
                        policyTypeEntity.getBaseValue(), travelDays, actualPersonCount, requiredAmount);

            } else {
                throw new IllegalArgumentException(
                        "Tipo de póliza no soportado para Wallet CASH: " + policyTypeId
                );
            }

        } else {
            throw new IllegalArgumentException(
                    "Tipo de wallet no soportado: " + wallet.getWalletType().getName()
            );
        }

        return requiredAmount;
    }

    private Wallet findValidCashWallet(List<Wallet> wallets) {
        // Sumar todos los saldos disponibles de las wallets CASH
        BigDecimal totalBalance = wallets.stream()
                .map(Wallet::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalBalance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "No tienes saldo disponible en tu billetera recargable. " +
                            "Por favor, recarga tu billetera para continuar."
            );
        }

        // Devolver la primera wallet con saldo positivo
        Wallet wallet = wallets.stream()
                .filter(w -> w.getTotal().compareTo(BigDecimal.ZERO) > 0)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No tienes saldo disponible en tu billetera recargable"
                ));

        log.info("✅ Wallet CASH encontrada: {} (Transaction ID: {}, saldo: ${})",
                wallet.getWalletId(), wallet.getTransactionId(), wallet.getTotal());

        return wallet;
    }

    private Wallet findValidPaxWallet(List<Wallet> wallets) {
        BigDecimal totalPax = wallets.stream()
                .map(Wallet::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPax.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "No tienes PAX disponibles en tu wallet. " +
                            "Por favor, compra un plan preferencial para continuar."
            );
        }

        Wallet wallet = wallets.stream()
                .filter(w -> w.getTotal().compareTo(BigDecimal.ZERO) > 0)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No tienes PAX disponibles en tu wallet"
                ));

        log.info("✅ Wallet PAX encontrada: {} (Transaction ID: {}, saldo: {} PAX)",
                wallet.getWalletId(), wallet.getTransactionId(), wallet.getTotal());

        return wallet;
    }

    /**
     * GUARDAR PÓLIZA CON MÚLTIPLES WALLETS (YA EXISTENTE - ACTUALIZADO)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected Policy savePolicyWithMultipleWallets(
            CreatePolicyRequest request,
            int actualPersonCount,
            MultipartFile primaryDataFile,
            MultipartFile[] otherAttachments,
            List<PersonPolicyRequest> validatedPersons,
            PolicyDataSource dataSource,
            String policyNumber,
            List<Wallet> wallets,
            BigDecimal totalRequired) throws IOException {

        log.info("💾 Guardando póliza usando {} wallet(s)...", wallets.size());

        if (policyRepository.existsByPolicyNumber(policyNumber)) {
            throw new IllegalStateException(
                    "El número de póliza " + policyNumber + " ya existe"
            );
        }

        // 1. Crear póliza
        Policy policy = createPolicyWithNumber(
                request,
                actualPersonCount,
                primaryDataFile != null,
                policyNumber
        );

        // 2. Agregar personas
        addPersonsToPolicy(policy.getPolicyId(), validatedPersons, dataSource);

        // 3. Guardar archivos
        saveFiles(policy.getPolicyId(), primaryDataFile, otherAttachments);

        // 4. Crear detalle de póliza
        createPolicyDetail(policy, request);

        // 5. Obtener información de la wallet para el tipo
        Long walletTypeId = wallets.get(0).getWalletType().getWalletTypeId();
        Long policyTypeId = request.getPolicyTypeId();

        // 6. Calcular valores monetarios y PAX
        long travelDays = java.time.temporal.ChronoUnit.DAYS.between(
                request.getDeparture().toLocalDate(),
                request.getArrival().toLocalDate()
        ) + 1;

        BigDecimal realMoneyAmount;
        BigDecimal paxQuantity = BigDecimal.ZERO;

        if (walletTypeId == 1L) {
            // PAX
            PolicyType policyType = policyTypeRepository.findById(policyTypeId)
                    .orElseThrow();
            paxQuantity = totalRequired;
            realMoneyAmount = policyType.getBaseValue()
                    .multiply(BigDecimal.valueOf(travelDays))
                    .multiply(BigDecimal.valueOf(actualPersonCount));
        } else if (walletTypeId == 2L) {
            // SUBSCRIPTION
            realMoneyAmount = BigDecimal.ZERO;
        } else {
            // CASH
            realMoneyAmount = totalRequired;
        }

        // 7. Crear Payment
        PaymentType paymentType = paymentTypeRepository.findById(request.getPaymentTypeId())
                .orElseThrow();

        BigDecimal amountToSave = (walletTypeId == 1L) ? paxQuantity : realMoneyAmount;

        Payment payment = new Payment(
                null,
                paymentType,
                PaymentStatus.COMPLETED,
                "WALLET-" + policyNumber,
                amountToSave,
                policy.getCreatedByUser(),
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );

        Payment savedPayment = paymentRepository.save(payment);

        // 8. Crear PolicyPayment
        PolicyPayment policyPayment = new PolicyPayment(
                null,
                savedPayment,
                policy,
                amountToSave,
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );

        policyPaymentRepository.save(policyPayment);

        // 9. Descontar de múltiples wallets (FIFO)
        BigDecimal remaining = totalRequired;
        int walletIndex = 0;

        for (Wallet wallet : wallets) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            walletIndex++;
            BigDecimal availableInWallet = wallet.getTotal();
            BigDecimal toDeduct = remaining.min(availableInWallet);

            // Descripción según tipo
            String description = buildTransactionDescription(
                    policyNumber,
                    walletIndex,
                    wallets.size(),
                    actualPersonCount,
                    travelDays,
                    policyTypeId,
                    walletTypeId,
                    toDeduct
            );

            // Crear transacción
            WalletTransaction transaction = new WalletTransaction(
                    null,
                    savedPayment,
                    wallet,
                    false, // EGRESO
                    toDeduct,
                    wallet.getTotal().subtract(toDeduct),
                    description,
                    ZonedDateTime.now(),
                    ZonedDateTime.now()
            );

            walletTransactionRepository.save(transaction);

            // Actualizar saldo
            BigDecimal newBalance = wallet.getTotal().subtract(toDeduct);
            walletRepository.updateBalance(
                    wallet.getWalletId(),
                    newBalance,
                    ZonedDateTime.now()
            );

            log.info("💰 Wallet #{} actualizada - ID: {}, Descontado: {}, Nuevo saldo: {}",
                    walletIndex, wallet.getWalletId(), toDeduct, newBalance);

            remaining = remaining.subtract(toDeduct);
        }

        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException(
                    "Error: Faltante después de procesar todas las wallets: " + remaining
            );
        }

        log.info("✅ Póliza {} completada usando {} wallet(s) - Tipo: {}",
                policyNumber, walletIndex, wallets.get(0).getWalletType().getName());

        return policy;
    }

    private String buildTransactionDescription(
            String policyNumber,
            int walletIndex,
            int totalWallets,
            int personCount,
            long travelDays,
            Long policyTypeId,
            Long walletTypeId,
            BigDecimal deductedAmount) {

        String walletPart = totalWallets > 1
                ? String.format(" - Parte %d/%d", walletIndex, totalWallets)
                : "";

        if (walletTypeId == 1L) {
            // PAX
            return String.format("Póliza %s%s - %d PAX (personas: %d × días: %d)",
                    policyNumber, walletPart, deductedAmount.intValue(),
                    personCount, travelDays);
        } else if (walletTypeId == 2L) {
            // SUBSCRIPTION
            return String.format("Póliza %s%s - %d habitaciones (Suscripción)",
                    policyNumber, walletPart, personCount);
        } else {
            // CASH
            if (policyTypeId == 2L) {
                long nights = travelDays - 1;
                return String.format("Póliza %s%s - $%s (%d personas × %d noches)",
                        policyNumber, walletPart, deductedAmount, personCount, nights);
            } else {
                return String.format("Póliza %s%s - $%s (%d personas × %d días)",
                        policyNumber, walletPart, deductedAmount, personCount, travelDays);
            }
        }
    }

    /**
     * 🆕 GENERAR NÚMERO DE PÓLIZA ÚNICO
     */
    private String generatePolicyNumber(Wallet wallet) {
        String transactionId = wallet.getTransactionId();
        int maxAttempts = 10; // Máximo de intentos (muy raro que falle)

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            // Generar código único de 5 caracteres alfanuméricos
            String uniqueCode = generateAlphanumericCode(5);

            // Formato: TRANSACTION_ID-UNIQUE_CODE
            String policyNumber = transactionId + "-" + uniqueCode;

            // Verificar si ya existe en la base de datos
            boolean exists = policyRepository.existsByPolicyNumber(policyNumber);

            if (!exists) {
                log.info("🎫 Número de póliza generado (intento {}): {}", attempt, policyNumber);
                return policyNumber;
            }

            log.warn("⚠️ Número de póliza {} ya existe (muy improbable), reintentando...", policyNumber);
        }

        // Si después de 10 intentos no se pudo generar un número único, lanzar excepción
        throw new IllegalStateException(
                String.format("No se pudo generar un número de póliza único después de %d intentos para wallet %d",
                        maxAttempts, wallet.getWalletId())
        );
    }

    private String generateAlphanumericCode(int length) {
        // Caracteres permitidos (sin ambigüedades: sin O, 0, I, 1, L)
        String allowedChars = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";
        Random random = new Random();
        StringBuilder code = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(allowedChars.length());
            code.append(allowedChars.charAt(index));
        }

        return code.toString();
    }

    /**
     * 🆕 GUARDAR PÓLIZA CON WALLET
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected Policy savePolicyWithWallet(
            CreatePolicyRequest request,
            int actualPersonCount,
            MultipartFile primaryDataFile,
            MultipartFile[] otherAttachments,
            List<PersonPolicyRequest> validatedPersons,
            PolicyDataSource dataSource,
            String policyNumber,
            Wallet wallet,
            BigDecimal requiredAmount) throws IOException {

        log.info("💾 Guardando póliza con WALLET (Tipo: {})...",
                wallet.getWalletType().getName());

        if (policyRepository.existsByPolicyNumber(policyNumber)) {
            throw new IllegalStateException(
                    "El número de póliza " + policyNumber + " ya existe en la base de datos. " +
                            "Por favor, intente nuevamente."
            );
        }

        // 1. Crear póliza CON número de póliza
        Policy policy = createPolicyWithNumber(
                request,
                actualPersonCount,
                primaryDataFile != null,
                policyNumber
        );

        // 2. Agregar personas a la póliza
        addPersonsToPolicy(policy.getPolicyId(), validatedPersons, dataSource);

        // 3. Guardar archivos
        saveFiles(policy.getPolicyId(), primaryDataFile, otherAttachments);

        // 4. Crear detalle de póliza
        createPolicyDetail(policy, request);

        // 5. Calcular días de viaje
        long travelDays = java.time.temporal.ChronoUnit.DAYS.between(
                request.getDeparture().toLocalDate(),
                request.getArrival().toLocalDate()
        ) + 1;

        Long policyTypeId = request.getPolicyTypeId();
        Long walletTypeId = wallet.getWalletType().getWalletTypeId();

        // 🔥 6. Calcular el monto REAL en dinero y la cantidad PAX
        BigDecimal realMoneyAmount;
        BigDecimal paxQuantity = BigDecimal.ZERO; // Nueva variable para guardar cantidad de PAX

        if (walletTypeId == 1L) {
            // ============================================
            // WALLET PAX (tipo 1): Guardar cantidad de PAX
            // ============================================
            PolicyType policyTypeEntity = policyTypeRepository.findById(policyTypeId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Tipo de póliza no encontrado: " + policyTypeId
                    ));

            // La cantidad de PAX consumidos
            paxQuantity = requiredAmount;

            // El valor real en dinero (para referencia)
            realMoneyAmount = policyTypeEntity.getBaseValue()
                    .multiply(BigDecimal.valueOf(travelDays))
                    .multiply(BigDecimal.valueOf(actualPersonCount));

            log.info("💵 {} PAX equivalen a ${} (precio: ${} × {} días × {} personas)",
                    paxQuantity, realMoneyAmount,
                    policyTypeEntity.getBaseValue(), travelDays, actualPersonCount);

        } else if (walletTypeId == 2L) {
            // ============================================
            // WALLET SUBSCRIPTION (tipo 2): Sin costo adicional
            // ============================================
            realMoneyAmount = BigDecimal.ZERO;
            log.info("💼 Suscripción: Póliza sin costo adicional (cubierta por suscripción)");

        } else if (walletTypeId == 3L) {
            // ============================================
            // WALLET CASH (tipo 3): Monto en dinero
            // ============================================
            realMoneyAmount = requiredAmount;
            log.info("💵 Wallet CASH: Monto ${}", realMoneyAmount);

        } else {
            throw new IllegalArgumentException(
                    "Tipo de wallet no soportado: " + wallet.getWalletType().getName()
            );
        }

        // 7. Crear Payment con el monto apropiado
        PaymentType paymentType = paymentTypeRepository.findById(request.getPaymentTypeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tipo de pago no encontrado: " + request.getPaymentTypeId()
                ));

        // 🔥 Para PAX: guardamos la cantidad de PAX, NO el valor en dinero
        BigDecimal amountToSave = (walletTypeId == 1L) ? paxQuantity : realMoneyAmount;

        Payment payment = new Payment(
                null,
                paymentType,
                PaymentStatus.COMPLETED,
                "WALLET-" + policyNumber,
                amountToSave, // 🔥 PAX quantity o money amount según el tipo
                policy.getCreatedByUser(),
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );

        Payment savedPayment = paymentRepository.save(payment);

        // 8. Crear PolicyPayment
        PolicyPayment policyPayment = new PolicyPayment(
                null,
                savedPayment,
                policy,
                amountToSave, // 🔥 PAX quantity o money amount según el tipo
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );

        policyPaymentRepository.save(policyPayment);

        // 9. Determinar descripción según tipo de wallet
        String description;

        if (walletTypeId == 1L) {
            // PAX: mostrar cantidad de PAX y valor equivalente
            description = String.format(
                    "Póliza %s - %d personas × %d días - %s PAX consumidos (equivalente a $%s)",
                    policyNumber, actualPersonCount, travelDays,
                    paxQuantity.toPlainString(), realMoneyAmount.toPlainString()
            );
        } else if (walletTypeId == 3L) {
            // CASH: mostrar monto en dinero
            if (policyTypeId == 2L) {
                long nights = travelDays - 1;
                description = String.format("Póliza %s - %d personas × %d noches - Monto: $%s",
                        policyNumber, actualPersonCount, nights, requiredAmount.toPlainString());
            } else {
                description = String.format("Póliza %s - %d personas × %d días - Monto: $%s",
                        policyNumber, actualPersonCount, travelDays, requiredAmount.toPlainString());
            }
        } else {
            // SUBSCRIPTION: mostrar habitaciones
            description = String.format("Póliza %s - %d habitaciones (Suscripción activa)",
                    policyNumber, actualPersonCount);
        }

        // 10. Crear WalletTransaction con cantidad de PAX o dinero
        WalletTransaction transaction = new WalletTransaction(
                null,
                savedPayment,
                wallet,
                false, // Es un EGRESO
                requiredAmount, // 🔥 Cantidad real descontada de la wallet (PAX, CASH o habitaciones)
                wallet.getTotal().subtract(requiredAmount),
                description,
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );

        walletTransactionRepository.save(transaction);

        // 11. Actualizar saldo de la wallet
        BigDecimal newBalance = wallet.getTotal().subtract(requiredAmount);
        walletRepository.updateBalance(wallet.getWalletId(), newBalance, ZonedDateTime.now());

        // Log según tipo de wallet
        if (walletTypeId == 1L) {
            log.info("💰 Wallet PAX actualizada. Saldo anterior: {} PAX, Consumido: {} PAX, Nuevo saldo: {} PAX",
                    wallet.getTotal(), paxQuantity, newBalance);
            log.info("📊 Registrado en Payment/PolicyPayment: {} PAX (equivalente a ${})",
                    paxQuantity, realMoneyAmount);
        } else if (walletTypeId == 2L) {
            log.info("💰 Wallet SUBSCRIPTION actualizada. Habitaciones anteriores: {}, Consumidas: {}, Restantes: {}",
                    wallet.getTotal(), requiredAmount, newBalance);
            log.info("💼 Póliza sin costo adicional (cubierta por suscripción)");
        } else if (walletTypeId == 3L) {
            log.info("💰 Wallet CASH actualizada. Saldo anterior: ${}, Consumido: ${}, Nuevo saldo: ${}",
                    wallet.getTotal(), requiredAmount, newBalance);
        }

        log.info("✅ Transacción de póliza {} completada con WALLET {}",
                policy.getPolicyNumber(), wallet.getWalletType().getName());

        return policy;
    }

    /**
     * 🆕 CREAR PÓLIZA CON NÚMERO
     */
    private Policy createPolicyWithNumber(
            CreatePolicyRequest request,
            int personCount,
            boolean hasFile,
            String policyNumber) {

        PolicyType policyType = policyTypeRepository.findById(request.getPolicyTypeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tipo de póliza no encontrado: " + request.getPolicyTypeId()
                ));

        Discount discount = null;
        if(request.getDiscount() != null && !request.getDiscount().trim().isEmpty()) {
            discount = discountService.findByName(request.getDiscount());
        }

        User createdBy = getAuthenticatedUser();

        Policy policy = new Policy(
                null,
                policyType,
                personCount,
                BigDecimal.ZERO,
                discount,
                policyNumber, // 🔥 NUEVO: Se establece desde el inicio
                createdBy,
                ZonedDateTime.now(),
                ZonedDateTime.now(),
                hasFile,
                null,
                null
        );

        Policy savedPolicy = policyRepository.save(policy);
        log.info("✅ Póliza {} creada con número {}", savedPolicy.getPolicyId(), policyNumber);

        return savedPolicy;
    }

    /**
     * ♻️ FLUJO ORIGINAL CON ZURICH (Renombrado)
     */
    private CreatePolicyResponse processZurichPayment(
            CreatePolicyRequest request,
            int actualPersonCount,
            MultipartFile primaryDataFile,
            MultipartFile[] otherAttachments,
            List<PersonPolicyRequest> validatedPersons,
            PolicyDataSource dataSource) throws IOException {

        // 🔥 GUARDAR PÓLIZA EN TRANSACCIÓN INDEPENDIENTE (SIEMPRE SE COMMITEA)
        Policy savedPolicy = savePolicyWithIndependentTransaction(
                request,
                actualPersonCount,
                primaryDataFile,
                otherAttachments,
                validatedPersons,
                dataSource
        );

        log.info("✅ Póliza {} guardada exitosamente en base de datos", savedPolicy.getPolicyId());

        // 🎯 INTENTAR GENERAR PAGO (FUERA DE LA TRANSACCIÓN)
        String paymentUrl = null;
        boolean paymentFailed = false;
        String paymentError = null;

        try {
            paymentUrl = generatePayment(request, savedPolicy.getPolicyId(), validatedPersons);
            log.info("✅ Pago generado exitosamente - URL: {}", paymentUrl);

        } catch (Exception paymentException) {
            paymentFailed = true;
            paymentError = paymentException.getMessage();
            log.error("❌ Error generando pago para póliza {} (pero la póliza YA ESTÁ GUARDADA): {}",
                    savedPolicy.getPolicyId(), paymentError, paymentException);
        }

        CreatePolicyResponse response = new CreatePolicyResponse(
                paymentUrl,
                policyResponseMapper.toDto(savedPolicy)
        );

        if (paymentFailed) {
            log.warn("⚠️ IMPORTANTE: Póliza {} creada exitosamente pero el pago falló.",
                    savedPolicy.getPolicyId());
        }

        return response;
    }

    /**
     * 🔥 MÉTODO CON TRANSACCIÓN INDEPENDIENTE
     * Usa REQUIRES_NEW para que se commitee independientemente del resultado del pago
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected Policy savePolicyWithIndependentTransaction(
            CreatePolicyRequest request,
            int actualPersonCount,
            MultipartFile primaryDataFile,
            MultipartFile[] otherAttachments,
            List<PersonPolicyRequest> validatedPersons,
            PolicyDataSource dataSource) throws IOException {

        log.info("💾 Guardando póliza en transacción independiente...");

        // 1. Crear póliza
        Policy savedPolicy = createPolicy(request, actualPersonCount, primaryDataFile != null);

        // 2. Agregar personas a la póliza
        addPersonsToPolicy(savedPolicy.getPolicyId(), validatedPersons, dataSource);

        // 3. Guardar archivos
        saveFiles(savedPolicy.getPolicyId(), primaryDataFile, otherAttachments);

        // 4. Crear detalle de póliza
        createPolicyDetail(savedPolicy, request);

        log.info("✅ Transacción de póliza {} completada y commiteada", savedPolicy.getPolicyId());

        return savedPolicy;
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

                        if (payment.getPayment().getStatus() != PaymentStatus.COMPLETED) {
                            return null;
                        }

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

    private PolicyDataSource detectDataSource(CreatePolicyRequest request, MultipartFile dataFile) {
        boolean hasManualPersons = request.getPersons() != null && !request.getPersons().isEmpty();
        boolean hasDataFile = dataFile != null && !dataFile.isEmpty();
        boolean hasPersonCount = request.getPersonCount() != null && request.getPersonCount() > 0;

        if (hasManualPersons && !hasDataFile) {
            return PolicyDataSource.MANUAL;
        }

        if (hasDataFile && !hasManualPersons) {
            String filename = dataFile.getOriginalFilename();
            if (filename == null) {
                throw new IllegalArgumentException("El archivo no tiene nombre");
            }

            String lowerFilename = filename.toLowerCase();

            if (lowerFilename.endsWith(".xlsx") || lowerFilename.endsWith(".xls")) {
                return PolicyDataSource.EXCEL;
            }

            if (lowerFilename.endsWith(".jpg") ||
                    lowerFilename.endsWith(".jpeg") ||
                    lowerFilename.endsWith(".png") ||
                    lowerFilename.endsWith(".pdf")) {

                if (!hasPersonCount) {
                    throw new IllegalArgumentException(
                            "Debe indicar la cantidad de personas (personCount) si sube una imagen o PDF"
                    );
                }

                return PolicyDataSource.IMAGE;
            }

            throw new IllegalArgumentException(
                    "Tipo de archivo no soportado. Use Excel (.xlsx/.xls) o imagen/PDF"
            );
        }

        if (hasManualPersons && hasDataFile) {
            throw new IllegalArgumentException(
                    "No puede enviar personas manualmente y archivo al mismo tiempo"
            );
        }

        throw new IllegalArgumentException(
                "Debe proporcionar personas manualmente o un archivo"
        );
    }

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
                log.info("📸 Procesando imagen/PDF como respaldo (sin OCR)");
                persons = createPersonsFromUserAndCount(request);
                break;

            default:
                throw new IllegalStateException("Fuente no soportada: " + dataSource);
        }

        if (persons == null || persons.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron personas válidas");
        }

        log.info("✅ {} personas extraídas y validadas desde {}",
                persons.size(), dataSource.getDisplayName());

        return persons;
    }

    private List<PersonPolicyRequest> createPersonsFromUserAndCount(CreatePolicyRequest request) {
        if (request.getPersonCount() == null || request.getPersonCount() <= 0) {
            throw new IllegalArgumentException(
                    "Debe proporcionar la cantidad de personas (personCount) cuando sube imágenes"
            );
        }

        User currentUser = getAuthenticatedUser();
        Person userPerson = personRepository.findById(currentUser.getPerson().getPersonId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró información de persona para el usuario actual"
                ));

        int personCount = request.getPersonCount();
        log.info("🔁 Generando {} registros desde usuario {} para póliza con imágenes",
                personCount, userPerson.getFullName());

        List<PersonPolicyRequest> persons = new ArrayList<>();
        for (int i = 0; i < personCount; i++) {
            // Generar nombre corto y descriptivo: "Imagen N" en lugar del nombre completo
            String shortName = String.format("Imagen %d", i + 1);

            persons.add(new PersonPolicyRequest(
                    shortName,
                    userPerson.getDocumentType() == DocumentType.NIT ? DocumentType.CC : userPerson.getDocumentType(),
                    userPerson.getDocumentNumber()
            ));
        }

        log.info("✅ {} personas generadas con nombres cortos para cálculo de pago", persons.size());
        return persons;
    }

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
                String validatedName = PersonValidator.validateAndNormalizeFullName(
                        person.getFullName()
                );

                if (person.getDocumentType() == null) {
                    throw new IllegalArgumentException("El tipo de documento no puede estar vacío");
                }

                if (!ALLOWED_DOCUMENT_TYPES.contains(person.getDocumentType())) {
                    throw new IllegalArgumentException(
                            "Tipo de documento no permitido: " + person.getDocumentType().getCode() +
                                    ". Solo se permiten: CC, Pasaporte, CE, NUIP (personas naturales)"
                    );
                }

                String validatedDocNumber = PersonValidator.validateDocumentNumber(
                        person.getDocumentNumber()
                );

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

    private List<PersonPolicyRequest> parseAndValidateExcel(MultipartFile excelFile) throws IOException {
        List<PersonPolicyRequest> list = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Set<String> seenDocuments = new HashSet<>();

        try (InputStream is = excelFile.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            log.info("📊 Procesando Excel - Total de filas: {}", sheet.getLastRowNum() + 1);

            for (int rowIndex = 12; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);

                if (row == null || isRowEmpty(row)) {
                    log.debug("⏭️ Saltando fila vacía: {}", rowIndex + 1);
                    continue;
                }

                int displayRowNumber = rowIndex + 1;

                try {
                    String docTypeStr = getCellString(row.getCell(0));
                    String docNumber = getCellString(row.getCell(1));
                    String fullName = getCellString(row.getCell(2));

                    if (docTypeStr == null || docTypeStr.trim().isEmpty()) {
                        errors.add("Fila " + displayRowNumber + ": Tipo de documento vacío");
                        continue;
                    }

                    if (docNumber == null || docNumber.trim().isEmpty()) {
                        errors.add("Fila " + displayRowNumber + ": Número de documento vacío");
                        continue;
                    }

                    if (fullName == null || fullName.trim().isEmpty()) {
                        errors.add("Fila " + displayRowNumber + ": Nombre completo vacío");
                        continue;
                    }

                    String validatedName = PersonValidator.validateAndNormalizeFullName(fullName);
                    DocumentType docType = DocumentType.fromValue(docTypeStr.trim());

                    if (!ALLOWED_DOCUMENT_TYPES.contains(docType)) {
                        errors.add("Fila " + displayRowNumber + ": Tipo de documento no permitido '" +
                                docType.getCode() + "'. Solo se permiten: CC (1), CE (2), Pasaporte (3), NUIP/TI (4)");
                        continue;
                    }

                    String validatedDocNumber = PersonValidator.validateDocumentNumber(docNumber);

                    String documentKey = docType + "-" + validatedDocNumber;
                    if (seenDocuments.contains(documentKey)) {
                        errors.add("Fila " + displayRowNumber + ": Documento duplicado " + documentKey);
                        continue;
                    }
                    seenDocuments.add(documentKey);

                    list.add(new PersonPolicyRequest(validatedName, docType, validatedDocNumber));

                    log.debug("✅ Fila {} procesada: {} - {} {} (código: {})",
                            displayRowNumber, validatedName, docType.getDisplayName(),
                            validatedDocNumber, docTypeStr);

                } catch (IllegalArgumentException e) {
                    errors.add("Fila " + displayRowNumber + ": " + e.getMessage());
                    log.warn("⚠️ Error en fila {}: {}", displayRowNumber, e.getMessage());
                } catch (Exception ex) {
                    errors.add("Fila " + displayRowNumber + ": Error inesperado - " + ex.getMessage());
                    log.error("❌ Error inesperado en fila {}: {}", displayRowNumber, ex.getMessage(), ex);
                }
            }
        }

        if (!errors.isEmpty()) {
            String errorMessage = "Errores encontrados en el Excel:\n" + String.join("\n", errors);
            log.error("❌ {}", errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        if (list.isEmpty()) {
            throw new IllegalArgumentException(
                    "El Excel no contiene personas válidas. " +
                            "Asegúrate de que:\n" +
                            "- Los datos comiencen en la fila 13 (fila 12 = cabeceras)\n" +
                            "- Columna A = Tipo de documento (1, 2, 3 o 4)\n" +
                            "- Columna B = Número de documento\n" +
                            "- Columna C = Nombre completo\n" +
                            "Referencia: 1=Cédula, 2=CE, 3=Pasaporte, 4=TI/NUIP"
            );
        }

        log.info("✅ Excel procesado correctamente: {} personas válidas", list.size());
        return list;
    }

    private Policy createPolicy(CreatePolicyRequest request, int personCount, boolean hasFile) {
        PolicyType policyType = policyTypeRepository.findById(request.getPolicyTypeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tipo de póliza no encontrado: " + request.getPolicyTypeId()
                ));

        Discount discount = null;

        if(request.getDiscount() != null && !request.getDiscount().trim().isEmpty()) {
            discount = discountService.findByName(request.getDiscount());
        }

        User createdBy = getAuthenticatedUser();

        Policy policy = new Policy(
                null,
                policyType,
                personCount,
                BigDecimal.ZERO,
                discount,
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

    private void addPersonsToPolicy(Long policyId, List<PersonPolicyRequest> persons,
                                    PolicyDataSource dataSource) {

        if (dataSource == PolicyDataSource.IMAGE) {
            log.info("⏭️ Saltando guardado de personas (imagen de respaldo - sin datos reales)");
            return;
        }

        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Póliza no encontrada: " + policyId));

        User currentUser = policy.getCreatedByUser();

        for (PersonPolicyRequest pr : persons) {
            Person person = personRepository
                    .findByDocument(pr.getDocumentType(), pr.getDocumentNumber())
                    .orElseGet(() -> personRepository.save(new Person(
                            null,
                            pr.getFullName(),
                            pr.getDocumentType(),
                            pr.getDocumentNumber(),
                            null,
                            ZonedDateTime.now(),
                            ZonedDateTime.now()
                    )));

            RelationshipType relationshipType = Objects.equals(
                    person.getPersonId(),
                    currentUser.getUserId()
            ) ? RelationshipType.HOLDER : RelationshipType.BENEFICIARY;

            policyPersonRepository.save(new PolicyPerson(
                    null,
                    policy,
                    person,
                    relationshipType,
                    ZonedDateTime.now(),
                    ZonedDateTime.now()
            ));
        }

        log.info("✅ {} personas agregadas correctamente a la póliza {}", persons.size(), policyId);
    }

    private void saveFiles(Long policyId, MultipartFile dataFile, MultipartFile[] attachments)
            throws IOException {

        int filesCount = 0;

        if (dataFile != null && !dataFile.isEmpty()) {
            fileAppService.uploadFileForPolicy(policyId, dataFile);
            filesCount++;
            log.info("✅ Archivo de datos guardado: {}", dataFile.getOriginalFilename());
        }

        if (attachments != null) {
            for (MultipartFile attachment : attachments) {
                if (attachment != null && !attachment.isEmpty()) {
                    fileAppService.uploadFileForPolicy(policyId, attachment);
                    filesCount++;
                    log.debug("✅ Adjunto guardado: {}", attachment.getOriginalFilename());
                }
            }
        }

        if (filesCount > 0) {
            log.info("✅ Total de {} archivo(s) guardado(s)", filesCount);
        } else {
            log.warn("⚠️ No se guardaron archivos");
        }
    }

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
            // Gateway/Zurich
            url = paymentService.createdPaymentWithZurich(paymentDTO);
            log.info("✅ Pago creado con Zurich para póliza {}", policyId);

        } else if (paymentDTO.getPaymentTypeId() == 2) {
            // Wallet PAX/SUBSCRIPTION - NO DEBERÍA LLEGAR AQUÍ
            log.warn("⚠️ PaymentTypeId 2 (Wallet PAX) no debería usar generatePayment");
            url = paymentService.cretaedPaymentWithWallet(paymentDTO);
            log.info("✅ Pago creado con Wallet PAX para póliza {}", policyId);

        } else if (paymentDTO.getPaymentTypeId() == 3) {
            // Wallet CASH - NO DEBERÍA LLEGAR AQUÍ
            log.warn("⚠️ PaymentTypeId 3 (Wallet CASH) no debería usar generatePayment");
            throw new IllegalArgumentException(
                    "El tipo de pago CASH (3) debe procesarse directamente con processWalletPayment, " +
                            "no requiere link de pago externo"
            );

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
        List<Policy> policies = policyRepository.findByCreatedByUserIdOrderByCreatedAtDesc(user.getUserId());

        if (policies.isEmpty()) {
            log.warn("⚠️ No hay pólizas para exportar");
            throw new IllegalArgumentException("No tiene pólizas registradas para exportar");
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Consolidado de Pólizas");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle moneyStyle = createMoneyStyle(workbook);

            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "Número Póliza",
                    "Tipo de Póliza",
                    "Cantidad Personas",
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

            int rowNum = 1;
            for (Policy policy : policies) {
                try {
                    if (policy.getPolicyNumber() == null || policy.getPolicyNumber().isEmpty()) {
                        continue;
                    }

                    Row row = sheet.createRow(rowNum++);

                    PolicyDetail detail = policyDetailRepository
                            .findByPolicyId(policy)
                            .orElse(null);

                    PolicyPayment payment = policyPaymentRepository
                            .findByPolicy(policy)
                            .orElse(null);

                    createCell(row, 0, policy.getPolicyNumber(), dataStyle);

                    String policyTypeName = policy.getPolicyType() != null
                            ? policy.getPolicyType().getName()
                            : "N/A";
                    createCell(row, 1, policyTypeName, dataStyle);

                    createCell(row, 2, String.valueOf(policy.getPersonCount()), dataStyle);

                    BigDecimal totalPrice = payment != null && payment.getAppliedAmount() != null
                            ? payment.getAppliedAmount(): BigDecimal.ZERO;
                    Cell totalPriceCell = row.createCell(3);
                    totalPriceCell.setCellValue(totalPrice.doubleValue());
                    totalPriceCell.setCellStyle(moneyStyle);

                    createCell(row, 4, detail != null ? detail.getOrigin() : "N/A", dataStyle);
                    createCell(row, 5, detail != null ? detail.getDestination() : "N/A", dataStyle);

                    if (detail != null && detail.getDeparture() != null) {
                        Cell dateCell = row.createCell(6);
                        dateCell.setCellValue(java.util.Date.from(detail.getDeparture().toInstant()));
                        dateCell.setCellStyle(dateStyle);
                    } else {
                        createCell(row, 6, "N/A", dataStyle);
                    }

                    if (detail != null && detail.getArrival() != null) {
                        Cell dateCell = row.createCell(7);
                        dateCell.setCellValue(java.util.Date.from(detail.getArrival().toInstant()));
                        dateCell.setCellStyle(dateStyle);
                    } else {
                        createCell(row, 7, "N/A", dataStyle);
                    }

                    if (policy.getCreatedAt() != null) {
                        Cell dateCell = row.createCell(8);
                        dateCell.setCellValue(java.util.Date.from(policy.getCreatedAt().toInstant()));
                        dateCell.setCellStyle(dateStyle);
                    } else {
                        createCell(row, 8, "N/A", dataStyle);
                    }

                    createCell(row, 9,
                            policy.getCreatedWithFile() != null && policy.getCreatedWithFile() ? "SÍ" : "NO",
                            dataStyle);

                } catch (Exception ex) {
                    log.error("❌ Error procesando póliza {}: {}",
                            policy.getPolicyId(), ex.getMessage());
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 500);
            }

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

        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> {
                    log.error("❌ Póliza no encontrada: {}", policyId);
                    return new IllegalArgumentException("Póliza no encontrada con ID: " + policyId);
                });

        User currentUser = getAuthenticatedUser();
        if (!policy.getCreatedByUser().getUserId().equals(currentUser.getUserId())) {
            log.error("❌ Usuario no autorizado para ver los asegurados de la póliza: {}", policyId);
            throw new SecurityException("No tiene permisos para ver los asegurados de esta póliza");
        }

        List<PolicyPerson> policyPersons = policyPersonRepository.findByPolicyId(policyId);

        if (policyPersons.isEmpty()) {
            log.info("📭 No se encontraron asegurados para la póliza: {}", policyId);
            return ApiResponse.success(
                    "No se encontraron asegurados para esta póliza",
                    List.of()
            );
        }

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

    @Override
    @Transactional(readOnly = true)
    public PolicyTypePriceResponse getPriceByPolicyTypeId(Long policyTypeId) {
        log.info("🔍 Obteniendo precio para PolicyType ID: {}", policyTypeId);

        PolicyType policyType = policyTypeRepository.findById(policyTypeId)
                .orElseThrow(() -> {
                    log.error("❌ PolicyType no encontrado con ID: {}", policyTypeId);
                    return new IllegalArgumentException(
                            "Tipo de póliza no encontrado: " + policyTypeId
                    );
                });

        if (!policyType.getActive()) {
            log.warn("⚠️ PolicyType {} está inactivo", policyTypeId);
            throw new IllegalArgumentException(
                    "El tipo de póliza no está activo: " + policyTypeId
            );
        }

        PolicyTypePriceResponse response = PolicyTypePriceResponse.builder()
                .policyTypeId(policyType.getPolicyTypeId())
                .name(policyType.getName())
                .baseValue(policyType.getBaseValue())
                .build();

        log.info("✅ Precio obtenido: {} - ${}", policyType.getName(), policyType.getBaseValue());

        return response;
    }

    private User getAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private static String getCellString(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case BLANK:
                return null;
            default:
                return null;
        }
    }

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