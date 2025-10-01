package com.safetrip.backend.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserRequest {
    String email;
    String phone;
    String password;

}
