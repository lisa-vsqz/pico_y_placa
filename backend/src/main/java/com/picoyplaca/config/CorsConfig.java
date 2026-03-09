package com.picoyplaca.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * Configuración de CORS para permitir comunicación entre el frontend Angular
 * y el backend Spring Boot.
 * <p>
 * Los orígenes permitidos se configuran vía propiedades/variables de entorno,
 * permitiendo flexibilidad entre ambientes (desarrollo vs producción).
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Parsear orígenes permitidos (separados por coma)
        for (String origin : allowedOrigins.split(",")) {
            config.addAllowedOrigin(origin.trim());
        }

        config.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "Accept"));
        config.setMaxAge(3600L); // Cache preflight por 1 hora

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);

        return new CorsFilter(source);
    }
}
