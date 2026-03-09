package com.picoyplaca.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI/Swagger para documentación interactiva de la API.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pico y Placa API")
                        .version("1.0.0")
                        .description("""
                                API REST para consultar la restricción vehicular de Pico y Placa.

                                Permite verificar si un vehículo puede circular en una fecha y hora
                                determinadas, basándose en el último dígito de la placa y las reglas
                                de restricción configuradas.

                                **Reglas actuales (Quito, Ecuador):**
                                - Lunes: placas terminadas en 1, 2
                                - Martes: placas terminadas en 3, 4
                                - Miércoles: placas terminadas en 5, 6
                                - Jueves: placas terminadas en 7, 8
                                - Viernes: placas terminadas en 9, 0

                                **Horarios de restricción:**
                                - 07:00 a 09:30
                                - 16:00 a 19:30
                                """)
                        .contact(new Contact()
                                .name("Pico y Placa Team"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Desarrollo local")));
    }
}
