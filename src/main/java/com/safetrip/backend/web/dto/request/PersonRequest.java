package com.safetrip.backend.web.dto.request;

import com.safetrip.backend.domain.model.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PersonRequest {
    private String fullName;
    private DocumentType documentType;
    private String documentNumber;
    private String address;
}
