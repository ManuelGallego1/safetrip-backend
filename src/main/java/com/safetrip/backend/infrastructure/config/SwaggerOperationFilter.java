package com.safetrip.backend.infrastructure.config;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

/**
 * Personaliza la documentación de Swagger agregando información de seguridad
 * basada en las anotaciones de Spring Security
 */
@Component
public class SwaggerOperationFilter implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        Method method = handlerMethod.getMethod();

        // Obtener información del rol requerido
        String requiredRole = extractRequiredRole(method, handlerMethod.getBeanType());

        if (requiredRole != null) {
            // Agregar descripción del rol requerido
            String currentDescription = operation.getDescription() != null ? operation.getDescription() : "";
            operation.setDescription(currentDescription +
                    "\n\n🔒 **Requiere rol:** " + requiredRole);
        } else {
            // Endpoint público
            String currentDescription = operation.getDescription() != null ? operation.getDescription() : "";
            operation.setDescription(currentDescription +
                    "\n\n🌐 **Acceso:** Público (sin autenticación requerida)");
        }

        return operation;
    }

    /**
     * Extrae el rol requerido del método o clase
     */
    private String extractRequiredRole(Method method, Class<?> beanType) {
        // Verificar anotación @PreAuthorize en el método
        PreAuthorize methodPreAuth = method.getAnnotation(PreAuthorize.class);
        if (methodPreAuth != null) {
            return extractRoleFromPreAuthorize(methodPreAuth.value());
        }

        // Verificar anotación @PreAuthorize en la clase
        PreAuthorize classPreAuth = beanType.getAnnotation(PreAuthorize.class);
        if (classPreAuth != null) {
            return extractRoleFromPreAuthorize(classPreAuth.value());
        }

        // Verificar si el método está en un controlador con rutas protegidas
        String requestPath = getRequestPath(method);
        return getRoleFromPath(requestPath);
    }

    /**
     * Extrae el rol de la expresión @PreAuthorize
     */
    private String extractRoleFromPreAuthorize(String expression) {
        if (expression.contains("ROLE_ADMIN") || expression.contains("'ADMIN'")) {
            return "ADMIN";
        }
        if (expression.contains("ROLE_CUSTOMER") || expression.contains("'CUSTOMER'")) {
            return "CUSTOMER";
        }
        return null;
    }

    /**
     * Determina el rol basado en el path del endpoint
     */
    private String getRoleFromPath(String path) {
        // Rutas de admin
        if (path.startsWith("/api/admin") ||
                path.startsWith("/api/credentials") ||
                path.startsWith("/api/discounts") ||
                path.startsWith("/api/files") ||
                (path.startsWith("/api/pdf") && !path.contains("policy"))) {
            return "ADMIN";
        }

        // Rutas de customer
        if (path.startsWith("/api/policies") && !path.contains("payment-confirmation") ||
                path.startsWith("/api/wallet-plans") && !path.contains("payment-confirmation") ||
                path.startsWith("/api/wallet-time-plans") && !path.contains("payment-confirmation") ||
                path.startsWith("/api/wallet-money") && !path.contains("payment-confirmation") ||
                path.startsWith("/api/wallets") ||
                path.startsWith("/api/payments")) {
            return "CUSTOMER";
        }

        // Rutas públicas
        if (path.startsWith("/api/auth") ||
                path.equals("/api/health") ||
                path.contains("payment-confirmation") ||
                path.startsWith("/api/policy-plans") ||
                path.equals("/api/policies/config")) {
            return null; // Público
        }

        // Por defecto, requiere autenticación
        return "AUTHENTICATED";
    }

    /**
     * Extrae el path de la request del método
     */
    private String getRequestPath(Method method) {
        // Verificar @RequestMapping
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        if (requestMapping != null && requestMapping.value().length > 0) {
            return requestMapping.value()[0];
        }

        // Verificar @GetMapping
        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        if (getMapping != null && getMapping.value().length > 0) {
            return getMapping.value()[0];
        }

        // Verificar @PostMapping
        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        if (postMapping != null && postMapping.value().length > 0) {
            return postMapping.value()[0];
        }

        // Verificar @PutMapping
        PutMapping putMapping = method.getAnnotation(PutMapping.class);
        if (putMapping != null && putMapping.value().length > 0) {
            return putMapping.value()[0];
        }

        // Verificar @DeleteMapping
        DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
        if (deleteMapping != null && deleteMapping.value().length > 0) {
            return deleteMapping.value()[0];
        }

        // Verificar @PatchMapping
        PatchMapping patchMapping = method.getAnnotation(PatchMapping.class);
        if (patchMapping != null && patchMapping.value().length > 0) {
            return patchMapping.value()[0];
        }

        // Intentar obtener del RequestMapping de la clase
        Class<?> controllerClass = method.getDeclaringClass();
        RequestMapping classMapping = controllerClass.getAnnotation(RequestMapping.class);
        if (classMapping != null && classMapping.value().length > 0) {
            return classMapping.value()[0];
        }

        return "";
    }
}