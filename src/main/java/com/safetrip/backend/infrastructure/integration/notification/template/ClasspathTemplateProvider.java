package com.safetrip.backend.infrastructure.integration.notification.template;

import com.safetrip.backend.domain.service.TemplateProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class ClasspathTemplateProvider implements TemplateProvider {

    @Override
    public String getOtpTemplate(String otp) {
        try {
            var resource = new ClassPathResource("templates/otp-verification.html");
            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return content.replace("{{OTP}}", otp);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar la plantilla de correo", e);
        }
    }
}