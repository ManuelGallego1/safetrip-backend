package com.safetrip.backend.web.dto.response;

import com.safetrip.backend.domain.model.User;
import com.safetrip.backend.domain.model.enums.DocumentType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    // Datos del User
    private String email;
    private String phone;
    private String roleName;
    private String profileImageUrl;

    // Datos de Person
    private Long personId;
    private String fullName;
    private DocumentType documentType;
    private String documentNumber;
    private String address;

    /**
     * Convierte un objeto de dominio User a UserResponse DTO
     */
    public static UserResponse fromDomain(User user) {
        if (user == null) {
            return null;
        }

        UserResponseBuilder builder = UserResponse.builder()
                .email(user.getEmail())
                .phone(user.getPhone())
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .profileImageUrl(user.getProfileImageUrl());

        // Incluir datos de Person si existen
        if (user.getPerson() != null) {
            builder
                    .personId(user.getPerson().getPersonId())
                    .fullName(user.getPerson().getFullName())
                    .documentType(user.getPerson().getDocumentType())
                    .documentNumber(user.getPerson().getDocumentNumber())
                    .address(user.getPerson().getAddress());
        }

        return builder.build();
    }
}