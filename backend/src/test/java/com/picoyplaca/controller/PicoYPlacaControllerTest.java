package com.picoyplaca.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.picoyplaca.domain.dto.PicoYPlacaRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración del controlador REST.
 * <p>
 * Verifica el comportamiento HTTP completo: serialización, validación,
 * códigos de respuesta y formato de errores.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("PicoYPlacaController - Integración HTTP")
class PicoYPlacaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String API_URL = "/api/v1/pico-y-placa";

    @Nested
    @DisplayName("POST /api/v1/pico-y-placa - Consultas exitosas")
    class SuccessfulQueries {

        @Test
        @DisplayName("Consulta válida retorna HTTP 200 con resultado")
        void validRequest_returns200() throws Exception {
            PicoYPlacaRequest request = new PicoYPlacaRequest(
                    "PBX-1234",
                    LocalDateTime.of(2030, 3, 11, 12, 0)); // lunes al mediodía

            mockMvc.perform(post(API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.plateNumber").value("PBX-1234"))
                    .andExpect(jsonPath("$.canDrive").isBoolean())
                    .andExpect(jsonPath("$.message").isString())
                    .andExpect(jsonPath("$.dateTime").exists());
        }

        @Test
        @DisplayName("Placa sin guión es aceptada y normalizada")
        void plateWithoutDash_accepted() throws Exception {
            PicoYPlacaRequest request = new PicoYPlacaRequest(
                    "PBX1234",
                    LocalDateTime.of(2030, 3, 11, 12, 0));

            mockMvc.perform(post(API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.plateNumber").value("PBX-1234"));
        }

        @Test
        @DisplayName("Placa en minúsculas es aceptada y normalizada a mayúsculas")
        void lowercasePlate_normalized() throws Exception {
            PicoYPlacaRequest request = new PicoYPlacaRequest(
                    "pbx-1234",
                    LocalDateTime.of(2030, 3, 11, 12, 0));

            mockMvc.perform(post(API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.plateNumber").value("PBX-1234"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/pico-y-placa - Errores de validación")
    class ValidationErrors {

        @Test
        @DisplayName("Placa vacía retorna HTTP 400")
        void emptyPlate_returns400() throws Exception {
            String json = """
                    {"plateNumber": "", "dateTime": "2030-03-11T12:00:00"}
                    """;

            mockMvc.perform(post(API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.fieldErrors").isArray());
        }

        @Test
        @DisplayName("Placa con formato inválido retorna HTTP 400")
        void invalidPlateFormat_returns400() throws Exception {
            String json = """
                    {"plateNumber": "12345", "dateTime": "2030-03-11T12:00:00"}
                    """;

            mockMvc.perform(post(API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors[0].field").value("plateNumber"));
        }

        @Test
        @DisplayName("Fecha nula retorna HTTP 400")
        void nullDate_returns400() throws Exception {
            String json = """
                    {"plateNumber": "PBX-1234"}
                    """;

            mockMvc.perform(post(API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Placa nula retorna HTTP 400")
        void nullPlate_returns400() throws Exception {
            String json = """
                    {"dateTime": "2030-03-11T12:00:00"}
                    """;

            mockMvc.perform(post(API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("JSON vacío retorna HTTP 400")
        void emptyJson_returns400() throws Exception {
            mockMvc.perform(post(API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("JSON malformado retorna HTTP 400")
        void malformedJson_returns400() throws Exception {
            mockMvc.perform(post(API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{invalid json}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
        }

        @Test
        @DisplayName("Fecha con formato inválido retorna HTTP 400")
        void invalidDateFormat_returns400() throws Exception {
            String json = """
                    {"plateNumber": "PBX-1234", "dateTime": "not-a-date"}
                    """;

            mockMvc.perform(post(API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/pico-y-placa - Errores de negocio")
    class BusinessErrors {

        @Test
        @DisplayName("Fecha en el pasado retorna HTTP 422")
        void pastDate_returns422() throws Exception {
            PicoYPlacaRequest request = new PicoYPlacaRequest(
                    "PBX-1234",
                    LocalDateTime.of(2020, 1, 1, 8, 0));

            mockMvc.perform(post(API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422))
                    .andExpect(jsonPath("$.error").value("DATE_IN_PAST"));
        }
    }

    @Nested
    @DisplayName("Métodos HTTP no soportados")
    class UnsupportedMethods {

        @Test
        @DisplayName("GET retorna HTTP 405")
        void getMethod_returns405() throws Exception {
            mockMvc.perform(get(API_URL))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("PUT retorna HTTP 405")
        void putMethod_returns405() throws Exception {
            mockMvc.perform(put(API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("DELETE retorna HTTP 405")
        void deleteMethod_returns405() throws Exception {
            mockMvc.perform(delete(API_URL))
                    .andExpect(status().isMethodNotAllowed());
        }
    }

    @Nested
    @DisplayName("Seguridad - Inputs adversariales")
    class SecurityTests {

        @Test
        @DisplayName("Placa con caracteres especiales es rechazada")
        void specialCharactersInPlate_rejected() throws Exception {
            String json = """
                    {"plateNumber": "<script>alert(1)</script>", "dateTime": "2030-03-11T12:00:00"}
                    """;

            mockMvc.perform(post(API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Placa con SQL injection es rechazada")
        void sqlInjectionInPlate_rejected() throws Exception {
            String json = """
                    {"plateNumber": "'; DROP TABLE users;--", "dateTime": "2030-03-11T12:00:00"}
                    """;

            mockMvc.perform(post(API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Placa extremadamente larga es rechazada")
        void veryLongPlate_rejected() throws Exception {
            String longPlate = "A".repeat(1000);
            String json = String.format(
                    "{\"plateNumber\": \"%s\", \"dateTime\": \"2030-03-11T12:00:00\"}", longPlate);

            mockMvc.perform(post(API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Body vacío retorna HTTP 400")
        void emptyBody_returns400() throws Exception {
            mockMvc.perform(post(API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(""))
                    .andExpect(status().isBadRequest());
        }
    }
}
