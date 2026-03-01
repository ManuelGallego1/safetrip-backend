package com.safetrip.backend.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class OpenApiConfig {

    @Value("${spring.application.name:SafeTrip API}")
    private String applicationName;

    private final SwaggerOperationFilter swaggerOperationFilter;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title(applicationName + " - Documentación API")
                        .version("1.0.6")
                        .description("API REST para la gestión de pólizas de seguros de viaje de SafeTrip. " +
                                "Esta API permite gestionar usuarios, pólizas, planes y pagos. ")
                        .contact(new Contact()
                                .name("SafeTrip Support")
                                .email("support@safetrip.com"))
                        .license(new License()
                                .name("Private License")
                                .url("https://safetrip.com/license")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Servidor Local"),
                        new Server().url("https://prod-api-safetrip.nevdata.cloud/").description("Servidor Producción"),
                        new Server().url("https://qa-api-safetrip.nevdata.cloud/").description("Servidor QA")
                ))

                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("🔑 Ingrese el token JWT obtenido del endpoint /api/auth/login. " +
                                                "Ejemplo: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'")
                        )
                );
    }

    /**
     * Registra el filtro que oculta operaciones según el rol del usuario
     */
    @Bean
    public OperationCustomizer operationCustomizer() {
        return swaggerOperationFilter;
    }
}