package com.safetrip.backend.web.dto.response;

import com.safetrip.backend.domain.model.enums.DocumentType;
import com.safetrip.backend.domain.model.enums.PaymentStatus;
import com.safetrip.backend.domain.model.enums.RelationshipType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Respuesta detallada de póliza para admin
 * Incluye toda la información: póliza, pago, personas, archivos, usuario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminPolicyDetailResponse {

    // Datos de la póliza
    private Long policyId;
    private String policyNumber;
    private Integer personCount;
    private String policyTypeName;
    private Long policyTypeId;
    private BigDecimal unitPriceWithDiscount;
    private String discountCode;
    private Boolean createdWithFile;
    private ZonedDateTime createdAt;

    // Detalles del viaje
    private String origin;
    private String destination;
    private ZonedDateTime departure;
    private ZonedDateTime arrival;

    // Información del pago
    private PaymentInfoDTO payment;

    // Usuario que creó la póliza
    private UserInfoDTO user;

    // Personas aseguradas
    private List<InsuredPersonDTO> insuredPersons;

    // Archivos adjuntos
    private List<FileInfoDTO> files;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentInfoDTO {
        private Long paymentId;
        private String transactionId;
        private BigDecimal amount;
        private PaymentStatus status;
        private String paymentTypeName;
        private ZonedDateTime paidAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoDTO {
        private Long userId;
        private String email;
        private String fullName;
        private String phone;
        private DocumentType documentType;
        private String documentNumber;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InsuredPersonDTO {
        private Long personId;
        private String fullName;
        private DocumentType documentType;
        private String documentNumber;
        private RelationshipType relationship;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileInfoDTO {
        private Long fileId;
        private String originalName;
        private String contentType;
        private Long size;
        private String downloadUrl;
        private ZonedDateTime uploadedAt;
    }
}