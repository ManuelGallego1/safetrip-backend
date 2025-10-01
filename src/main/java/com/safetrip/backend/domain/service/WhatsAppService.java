package com.safetrip.backend.domain.service;

public interface WhatsAppService {
    void sendOTP(String phone, String otp);
}