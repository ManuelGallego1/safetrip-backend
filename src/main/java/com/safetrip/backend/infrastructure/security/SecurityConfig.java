package com.safetrip.backend.infrastructure.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods}")
    private String allowedMethods;

    @Value("${cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${cors.exposed-headers}")
    private String exposedHeaders;

    @Value("${cors.max-age}")
    private Long maxAge;

    @Value("${springdoc.swagger-ui.enabled:true}")
    private boolean swaggerEnabled;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    // Permitir preflight
                    auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

                    // Endpoints de autenticación
                    auth.requestMatchers("/api/auth/**").permitAll();
                    auth.requestMatchers("/api/health").permitAll();

                    // Página de login de Swagger y recursos estáticos
                    auth.requestMatchers("/swagger-login.html", "/swagger-auth-injector.js").permitAll();
                    auth.requestMatchers("/", "/favicon.ico").permitAll();
                    auth.requestMatchers("/swagger-resources/**", "/webjars/**").permitAll();

                    if (swaggerEnabled) {
                        auth.requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml"
                        ).hasRole("ADMIN");
                    }

                    // Endpoints de políticas
                    auth.requestMatchers("/api/policies/payment-confirmation").permitAll();
                    auth.requestMatchers("/api/policy-plans/**").permitAll();
                    auth.requestMatchers("/api/policies/config").permitAll();
                    auth.requestMatchers("/api/policies/**").hasAnyRole("CUSTOMER");

                    // Endpoints de wallet
                    auth.requestMatchers("/api/wallet-plans/payment-confirmation").permitAll();
                    auth.requestMatchers("/api/wallet-plans/**").hasRole("CUSTOMER");
                    auth.requestMatchers("/api/wallet-time-plans/payment-confirmation").permitAll();
                    auth.requestMatchers("/api/wallet-time-plans/**").hasRole("CUSTOMER");
                    auth.requestMatchers("/api/wallet-money/payment-confirmation").permitAll();
                    auth.requestMatchers("/api/wallet-money/**").hasRole("CUSTOMER");

                    // Endpoints administrativos
                    auth.requestMatchers("/api/credentials/**").hasRole("ADMIN");
                    auth.requestMatchers("/api/admin/policies").hasAnyRole("SUPPORT", "ADMIN");
                    auth.requestMatchers("/api/admin/**").hasRole("ADMIN");
                    auth.requestMatchers("/api/files/**").hasRole("ADMIN");
                    auth.requestMatchers("/api/pdf/**").permitAll();
                    auth.requestMatchers("/api/discounts/**").hasRole("ADMIN");

                    // Cualquier otra solicitud requiere autenticación
                    auth.anyRequest().authenticated();
                })
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            String requestURI = request.getRequestURI();

                            // Si intenta acceder a Swagger sin auth, redirigir al login
                            if (requestURI.contains("/swagger-ui") ||
                                    requestURI.equals("/swagger-ui.html") ||
                                    requestURI.contains("/v3/api-docs")) {
                                response.sendRedirect("/swagger-login.html");
                            } else {
                                // Para APIs, devolver 401
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json");
                                response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Token JWT requerido\"}");
                            }
                        })
                )
                // ⚠️ CRÍTICO: El orden de los filtros importa
                // 1. Primero convierte la cookie en header
                .addFilterBefore(cookieToHeaderFilter(), UsernamePasswordAuthenticationFilter.class)
                // 2. Luego valida el JWT
                .addFilterAfter(jwtFilter, CookieToHeaderFilter.class);

        return http.build();
    }

    @Bean
    public CookieToHeaderFilter cookieToHeaderFilter() {
        return new CookieToHeaderFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration defaultConfig = new CorsConfiguration();
        defaultConfig.setAllowedOriginPatterns(parseCommaSeparated(allowedOrigins));
        defaultConfig.setAllowedMethods(parseCommaSeparated(allowedMethods));
        defaultConfig.setAllowedHeaders(parseCommaSeparated(allowedHeaders));
        defaultConfig.setExposedHeaders(parseCommaSeparated(exposedHeaders));
        defaultConfig.setAllowCredentials(true);
        defaultConfig.setMaxAge(maxAge);

        CorsConfiguration paymentConfirmationConfig = new CorsConfiguration();
        paymentConfirmationConfig.setAllowedOriginPatterns(List.of("*"));
        paymentConfirmationConfig.setAllowedMethods(parseCommaSeparated(allowedMethods));
        paymentConfirmationConfig.setAllowedHeaders(List.of("*"));
        paymentConfirmationConfig.setAllowCredentials(false);
        paymentConfirmationConfig.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/policies/payment-confirmation", paymentConfirmationConfig);
        source.registerCorsConfiguration("/api/wallet-time-plans/payment-confirmation", paymentConfirmationConfig);
        source.registerCorsConfiguration("/api/wallet-plans/payment-confirmation", paymentConfirmationConfig);
        source.registerCorsConfiguration("/api/wallet-money/payment-confirmation", paymentConfirmationConfig);
        source.registerCorsConfiguration("/**", defaultConfig);

        return source;
    }

    private List<String> parseCommaSeparated(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .toList();
    }
}