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

    @Value("${pdf.template.policy01:classpath:templates/template01Ap.pdf}")
    private String template01Path;

    @Value("${pdf.template.policy02:classpath:templates/template02Ap.pdf}")
    private String template02Path;

    private Resource template01Resource;
    private Resource template02Resource;

    @PostConstruct
    public void init() {
        this.template01Resource = resourceLoader.getResource(template01Path);
        this.template02Resource = resourceLoader.getResource(template02Path);
    }
}