package com.gradecalculator.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradecalculator.dto.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Custom security filter that enforces a secure API key check (via header 'X-API-KEY')
 * for all internal system-level microservice endpoints under '/api/internal/**'.
 */
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-KEY";

    @Value("${app.api.key:secure-internal-api-key}")
    private String configuredApiKey;

    @org.springframework.beans.factory.annotation.Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Only protect endpoints under /api/internal/
        if (path.startsWith("/api/internal/")) {
            String requestApiKey = request.getHeader(API_KEY_HEADER);

            if (requestApiKey == null || requestApiKey.trim().isEmpty() || !requestApiKey.equals(configuredApiKey)) {
                logger.warn(String.format("Security Warning: Unauthorized internal service request to '%s' from IP: '%s' with invalid or missing API Key.", path, request.getRemoteAddr()));
                
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                ErrorResponse error = new ErrorResponse(
                        "UNAUTHORIZED",
                        HttpStatus.UNAUTHORIZED.value(),
                        "Missing or invalid API key in header X-API-KEY.",
                        path,
                        LocalDateTime.now(),
                        List.of("The requested system-to-system endpoint requires valid X-API-KEY authentication.")
                );

                response.getWriter().write(objectMapper.writeValueAsString(error));
                return;
            }

            // Valid key: Set an internal authority principal in SecurityContext
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    "internal-system",
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_INTERNAL"), new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
