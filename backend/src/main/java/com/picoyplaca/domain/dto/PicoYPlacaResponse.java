package com.picoyplaca.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO de respuesta con el resultado de la consulta de Pico y Placa.
 * <p>
 * Nota de seguridad: solo se retorna el último dígito de la placa
 * en la respuesta, no la placa completa, para minimizar la exposición
 * de información del vehículo.
 */
@Schema(description = "Resultado de la consulta de restricción vehicular")
public record PicoYPlacaResponse(

        @Schema(description = "Número de placa consultada (formato normalizado)", example = "PBX-1234") String plateNumber,

        @Schema(description = "Fecha y hora consultada", example = "2026-03-09T08:30:00") LocalDateTime dateTime,

        @Schema(description = "Indica si el vehículo puede circular", example = "true") boolean canDrive,

        @Schema(description = "Mensaje descriptivo del resultado", example = "El vehículo con placa PBX-1234 PUEDE circular el lunes 09/03/2026 a las 08:30.") String message

) {
}
