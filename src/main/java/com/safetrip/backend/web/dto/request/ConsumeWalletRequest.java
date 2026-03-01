package com.safetrip.backend.web.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsumeWalletRequest {

    @NotNull(message = "La póliza es requerida")
    private Long policyId;

    @NotNull(message = "El tipo de wallet es requerido")
    private Long walletTypeId; // 1 = PAX, 2 = CASH

    @NotNull(message = "La fecha de salida es requerida")
    private ZonedDateTime departure;

    @NotNull(message = "La fecha de llegada es requerida")
    private ZonedDateTime arrival;

    @NotNull(message = "Las personas son requeridas")
    private List<PersonRequest> persons;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonRequest {
        @NotNull
        private String fullName;
        @NotNull
        private String documentNumber;
    }
}