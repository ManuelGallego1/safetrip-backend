package com.safetrip.backend.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PersonRequest {
    String fullName;
    String documentType;
    String documentNumber;
    String address;
}
