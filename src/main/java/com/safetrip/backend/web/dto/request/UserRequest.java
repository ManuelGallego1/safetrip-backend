package com.safetrip.backend.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserRequest {
    String fullName;
    String email;
    String phoneNumber;
    String address;
    String documentNumber;
}
