package com.safetrip.backend.infrastructure.security;

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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // ✅ permite preflight
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers("/api/policies/payment-confirmation").permitAll()
                        .requestMatchers("/api/credentials/**").hasRole("ADMIN")
                        .requestMatchers("/api/policies/**").hasRole("CUSTOMER")
                        .requestMatchers("/api/pdf/**").permitAll()
                        .anyRequest().authenticated()
                )
                // ✅ muy importante: poner el filtro JWT después del CORS
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
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
        paymentConfirmationConfig.setAllowCredentials(false); // ⚠️ Debe ser false cuando usamos "*"
        paymentConfirmationConfig.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/policies/payment-confirmation", paymentConfirmationConfig); // ✅ Específico
        source.registerCorsConfiguration("/**", defaultConfig); // ✅ Para el resto

        return source;
    }

    /**
     * Parsea una cadena separada por comas en una lista
     * Ejemplo: "GET,POST,PUT" -> ["GET", "POST", "PUT"]
     */
    private List<String> parseCommaSeparated(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .toList();
    }
}