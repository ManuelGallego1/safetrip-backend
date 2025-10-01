package com.safetrip.backend.infrastructure.integration.notification.whatsapp;

import com.safetrip.backend.domain.service.WhatsAppService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class WhatsAppServiceImpl implements WhatsAppService {

    private final WebClient webClient;
    private final String apiKey;

    public WhatsAppServiceImpl(@Value("${whatsapp.url}") String baseUrl,
                               @Value("${whatsapp.api-key}") String apiKey) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    @Override
    public void sendOTP(String phone, String otp) {
        webClient.post()
                .uri("/send-otp")
                .header("x-api-key", apiKey)
                .bodyValue(new OTPRequest(phone, otp))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    private record OTPRequest(String phone, String otp) {}
}