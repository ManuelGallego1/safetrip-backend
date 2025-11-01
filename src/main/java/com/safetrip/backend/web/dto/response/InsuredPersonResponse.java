package com.safetrip.backend.web.dto.response;

import com.safetrip.backend.domain.model.enums.DocumentType;
import com.safetrip.backend.domain.model.enums.RelationshipType;

import java.time.ZonedDateTime;

public record InsuredPersonResponse(
        Long personId,
        String fullName,
        DocumentType documentType,
        String documentNumber,
        RelationshipType relationship,
        String relationshipDisplay,
        ZonedDateTime addedAt
) {
    public InsuredPersonResponse(
            Long personId,
            String fullName,
            DocumentType documentType,
            String documentNumber,
            RelationshipType relationship,
            ZonedDateTime addedAt
    ) {
        this(
                personId,
                fullName,
                documentType,
                documentNumber,
                relationship,
                relationship.name(),
                addedAt
        );
    }
}