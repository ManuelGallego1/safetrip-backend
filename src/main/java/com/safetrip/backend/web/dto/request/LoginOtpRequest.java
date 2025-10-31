package com.safetrip.backend.web.dto.request;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginOtpRequest {
    String otp;
    String phone;
}