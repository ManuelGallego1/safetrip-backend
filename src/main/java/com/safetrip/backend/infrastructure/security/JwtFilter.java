package com.safetrip.backend.infrastructure.security;

import com.safetrip.backend.domain.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, jakarta.servlet.ServletException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtService.validateToken(token)) {
                String phone = jwtService.getPhoneFromToken(token);
                userRepository.findByPhone(phone).ifPresent(user -> {
                    String roleName = "ROLE_" + user.getRole().getName().toUpperCase();
                    var authorities = List.of(new SimpleGrantedAuthority(roleName));

                    log.info("Authenticated user: {} with authorities: {}", phone, authorities);

                    var auth = new UsernamePasswordAuthenticationToken(
                            user, null, authorities
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            }
        }

        filterChain.doFilter(request, response);
    }
}