package com.picoyplaca.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO estándar para respuestas de error de la API.
 * <p>
 * Proporciona información estructurada y consistente sobre errores,
 * sin exponer detalles internos del sistema.
 */
@Schema(description = "Respuesta de error de la API")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(

        @Schema(description = "Código de estado HTTP", example = "400") int status,

        @Schema(description = "Tipo de error", example = "VALIDATION_ERROR") String error,

        @Schema(description = "Mensaje descriptivo del error", example = "Los datos de entrada son inválidos") String message,

        @Schema(description = "Marca de tiempo del error") LocalDateTime timestamp,

        @Schema(description = "Lista de errores de validación específicos") List<FieldError> fieldErrors

) {
    /**
     * Crea una respuesta de error sin errores de campo.
     */
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(status, error, message, LocalDateTime.now(), null);
    }

    /**
     * Crea una respuesta de error con errores de validación de campo.
     */
    public static ErrorResponse ofValidation(int status, String message, List<FieldError> fieldErrors) {
        return new ErrorResponse(status, "VALIDATION_ERROR", message, LocalDateTime.now(), fieldErrors);
    }

    /**
     * Detalle de error de un campo específico.
     */
    @Schema(description = "Detalle de error de validación de un campo")
    public record FieldError(
            @Schema(description = "Nombre del campo con error", example = "plateNumber") String field,

            @Schema(description = "Mensaje de error del campo", example = "Formato de placa inválido. Use el formato ABC-1234 o ABC1234") String message) {
    }
}
