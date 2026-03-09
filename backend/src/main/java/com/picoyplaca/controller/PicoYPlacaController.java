package com.picoyplaca.controller;

import com.picoyplaca.domain.dto.ErrorResponse;
import com.picoyplaca.domain.dto.PicoYPlacaRequest;
import com.picoyplaca.domain.dto.PicoYPlacaResponse;
import com.picoyplaca.service.PicoYPlacaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para consultas de Pico y Placa.
 * <p>
 * Responsabilidad única: recibir la petición HTTP, delegar la lógica
 * al servicio, y retornar la respuesta. No contiene lógica de negocio.
 */
@RestController
@RequestMapping("/api/v1/pico-y-placa")
@Tag(name = "Pico y Placa", description = "Operaciones de consulta de restricción vehicular")
public class PicoYPlacaController {

    private static final Logger log = LoggerFactory.getLogger(PicoYPlacaController.class);

    private final PicoYPlacaService picoYPlacaService;

    public PicoYPlacaController(PicoYPlacaService picoYPlacaService) {
        this.picoYPlacaService = picoYPlacaService;
    }

    /**
     * Consulta si un vehículo puede circular según las reglas de Pico y Placa.
     *
     * @param request datos de la consulta (placa, fecha y hora)
     * @return resultado indicando si puede circular o no
     */
    @Operation(summary = "Consultar restricción de Pico y Placa", description = """
            Verifica si un vehículo puede circular en una fecha y hora determinadas,
            basándose en el último dígito de la placa y las reglas de Pico y Placa vigentes.

            **Validaciones:**
            - La placa debe tener formato ecuatoriano (ABC-1234 o ABC1234)
            - La fecha no puede ser anterior a la fecha/hora actual

            **Regla:** Un vehículo NO puede circular si su último dígito coincide con
            el día de restricción Y la hora está dentro de las franjas horarias restringidas.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta procesada exitosamente", content = @Content(schema = @Schema(implementation = PicoYPlacaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (placa malformada, fecha incorrecta)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "La fecha proporcionada es anterior a la actual", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PicoYPlacaResponse> checkRestriction(
            @Valid @RequestBody PicoYPlacaRequest request) {

        log.info("Recibida consulta de Pico y Placa para placa: {}",
                maskPlate(request.plateNumber()));

        PicoYPlacaResponse response = picoYPlacaService.checkRestriction(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Enmascara la placa para logging seguro.
     * Solo muestra los primeros 3 caracteres y el último dígito.
     * Ejemplo: PBX-1234 → PBX-***4
     */
    private String maskPlate(String plate) {
        if (plate == null || plate.length() < 4) {
            return "***";
        }
        String cleaned = plate.toUpperCase().trim();
        return cleaned.substring(0, 3) + "-***" + cleaned.charAt(cleaned.length() - 1);
    }
}
