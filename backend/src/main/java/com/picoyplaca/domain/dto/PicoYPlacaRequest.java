package com.picoyplaca.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

/**
 * DTO de entrada para la consulta de Pico y Placa.
 * <p>
 * Contiene las validaciones de formato necesarias para garantizar
 * que los datos de entrada son correctos antes de procesarlos.
 * <p>
 * Nota de seguridad: la placa se valida con regex estricto para
 * prevenir inyección de datos maliciosos.
 */
@Schema(description = "Datos de consulta para verificar restricción de Pico y Placa")
public record PicoYPlacaRequest(

        @Schema(description = "Número de placa del vehículo (formato Ecuador: ABC-1234 o ABC1234)", example = "PBX-1234", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "El número de placa es obligatorio") @Pattern(regexp = "^[A-Za-z]{3}-?\\d{3,4}$", message = "Formato de placa inválido. Use el formato ABC-1234 o ABC1234") String plateNumber,

        @Schema(description = "Fecha y hora para la consulta (no puede ser anterior a la fecha/hora actual)", example = "2026-03-09T08:30:00", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "La fecha y hora son obligatorias") LocalDateTime dateTime

) {
    /**
     * Retorna la placa normalizada en mayúsculas y con guión.
     */
    public String normalizedPlate() {
        String upper = plateNumber.toUpperCase().trim();
        // Si no tiene guión, insertarlo en la posición correcta
        if (!upper.contains("-") && upper.length() >= 4) {
            return upper.substring(0, 3) + "-" + upper.substring(3);
        }
        return upper;
    }

    /**
     * Extrae el último dígito de la placa.
     */
    public int lastDigit() {
        String cleaned = plateNumber.replaceAll("[^0-9]", "");
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("La placa no contiene dígitos numéricos");
        }
        return Character.getNumericValue(cleaned.charAt(cleaned.length() - 1));
    }
}
