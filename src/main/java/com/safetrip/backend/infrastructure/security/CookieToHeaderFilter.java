package com.safetrip.backend.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Filtro que convierte la cookie 'swagger_auth_token' en el header 'Authorization'
 * Esto permite que Spring Security reconozca el token guardado en la cookie
 */
public class CookieToHeaderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Buscar la cookie con el token
        String token = getTokenFromCookie(request);

        if (token != null && !token.isEmpty()) {
            // Envolver el request para agregar el header Authorization
            HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {
                @Override
                public String getHeader(String name) {
                    if ("Authorization".equalsIgnoreCase(name)) {
                        return "Bearer " + token;
                    }
                    return super.getHeader(name);
                }

                @Override
                public Enumeration<String> getHeaders(String name) {
                    if ("Authorization".equalsIgnoreCase(name)) {
                        return Collections.enumeration(Collections.singletonList("Bearer " + token));
                    }
                    return super.getHeaders(name);
                }

                @Override
                public Enumeration<String> getHeaderNames() {
                    Map<String, String> headers = new HashMap<>();
                    Enumeration<String> originalHeaders = super.getHeaderNames();
                    while (originalHeaders.hasMoreElements()) {
                        String headerName = originalHeaders.nextElement();
                        headers.put(headerName, super.getHeader(headerName));
                    }
                    headers.put("Authorization", "Bearer " + token);
                    return Collections.enumeration(headers.keySet());
                }
            };

            filterChain.doFilter(wrappedRequest, response);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Extrae el token de la cookie 'swagger_auth_token'
     */
    private String getTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("swagger_auth_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}