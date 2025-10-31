package com.safetrip.backend.infrastructure.integration.pdf.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import jakarta.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
@Getter
public class PdfConfig {

    private final ResourceLoader resourceLoader;

    @Value("${pdf.template.policy:classpath:templates/template-hotel-policy-page-1.pdf}")
    private String policyTemplatePath;

    private Resource policyTemplateResource;

    @PostConstruct
    public void init() {
        this.policyTemplateResource = resourceLoader.getResource(policyTemplatePath);
    }
}