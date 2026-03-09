package com.picoyplaca.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de seguridad que agrega cabeceras HTTP de protección.
 * <p>
 * Estas cabeceras ayudan a mitigar vulnerabilidades comunes como:
 * - XSS (Cross-Site Scripting)
 * - Clickjacking
 * - MIME type sniffing
 * - Information disclosure
 */
@Component
@Order(1)
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // Protección contra XSS
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // Protección contra clickjacking
        response.setHeader("X-Frame-Options", "DENY");

        // Política de referencia
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Deshabilitar cache para respuestas de API
        if (request.getRequestURI().startsWith("/api/")) {
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
            response.setHeader("Pragma", "no-cache");
        }

        // Ocultar información del servidor
        response.setHeader("Server", "");

        filterChain.doFilter(request, response);
    }
}
