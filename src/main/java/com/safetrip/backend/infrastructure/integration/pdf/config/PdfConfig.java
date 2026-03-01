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

    @Value("${pdf.template.policy01Ap:classpath:templates/template01Ap.pdf}")
    private String template01Path;

    @Value("${pdf.template.policy02Ap:classpath:templates/template02Ap.pdf}")
    private String template02Path;

    @Value("${pdf.template.policy01Sh:classpath:templates/template01Sh.pdf}")
    private String template01ShPath;

    @Value("${pdf.template.policy02Sh:classpath:templates/template02Sh.pdf}")
    private String template02ShPath;

    @Value("${pdf.template.policy02File:classpath:templates/template02File.pdf}")
    private String template02FilePath;

    @Value("${pdf.template.policy02ShPref:classpath:templates/template02ShPref.pdf}")
    private String template02ShPrefPath;

    @Value("${pdf.template.policy01ApPax:classpath:templates/template01ApPax.pdf}")
    private String template01ApPaxPath;

    @Value("${pdf.template.policy02ApPax:classpath:templates/template02ApPax.pdf}")
    private String template02ApPaxPath;

    private Resource template01Resource;
    private Resource template02Resource;
    private Resource template01ShResource;
    private Resource template02ShResource;
    private Resource template02FileResource;
    private Resource template02ShPrefResource;
    private Resource template01ApPaxResource;
    private Resource template02ApPaxResource;

    @PostConstruct
    public void init() {
        this.template01Resource = resourceLoader.getResource(template01Path);
        this.template02Resource = resourceLoader.getResource(template02Path);
        this.template01ShResource = resourceLoader.getResource(template01ShPath);
        this.template02ShResource = resourceLoader.getResource(template02ShPath);
        this.template02FileResource = resourceLoader.getResource(template02FilePath);
        this.template02ShPrefResource = resourceLoader.getResource(template02ShPrefPath);
        this.template01ApPaxResource = resourceLoader.getResource(template01ApPaxPath);
        this.template02ApPaxResource = resourceLoader.getResource(template02ApPaxPath);
    }
}