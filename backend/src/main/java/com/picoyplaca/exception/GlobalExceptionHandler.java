package com.picoyplaca.exception;

import com.picoyplaca.domain.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

/**
 * Manejador global de excepciones para toda la API.
 * <p>
 * Centraliza el manejo de errores para garantizar:
 * - Respuestas HTTP consistentes y correctas
 * - No exposición de stack traces o información interna
 * - Logging adecuado de errores para debugging
 * - Formato de error estándar en toda la API
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja errores de validación de campos (@Valid).
     * HTTP 400 - Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        log.warn("Error de validación: {}", ex.getMessage());

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ErrorResponse.FieldError(
                        error.getField(),
                        error.getDefaultMessage()))
                .toList();

        ErrorResponse response = ErrorResponse.ofValidation(
                HttpStatus.BAD_REQUEST.value(),
                "Los datos de entrada son inválidos. Por favor, revise los campos.",
                fieldErrors);

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Maneja errores de formato en el cuerpo de la request (JSON malformado, tipos
     * incorrectos).
     * HTTP 400 - Bad Request
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Error al leer el cuerpo de la petición: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                "El formato de la petición es inválido. Verifique que el JSON sea correcto y que los tipos de datos sean adecuados.");

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Maneja fecha en el pasado (regla de negocio).
     * HTTP 422 - Unprocessable Entity
     */
    @ExceptionHandler(DateInPastException.class)
    public ResponseEntity<ErrorResponse> handleDateInPast(DateInPastException ex) {
        log.info("Consulta rechazada - fecha en el pasado: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "DATE_IN_PAST",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    /**
     * Maneja argumentos ilegales (por ejemplo, placa sin dígitos).
     * HTTP 400 - Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Argumento inválido: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_ARGUMENT",
                ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Maneja métodos HTTP no soportados.
     * HTTP 405 - Method Not Allowed
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("Método HTTP no soportado: {}", ex.getMethod());

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "METHOD_NOT_ALLOWED",
                String.format("El método %s no está soportado para esta ruta.", ex.getMethod()));

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    /**
     * Maneja rutas no encontradas.
     * HTTP 404 - Not Found
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoResourceFoundException ex) {
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "NOT_FOUND",
                "El recurso solicitado no existe.");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Maneja cualquier excepción no prevista.
     * HTTP 500 - Internal Server Error
     * <p>
     * IMPORTANTE: No expone el mensaje de la excepción al cliente
     * para evitar filtrar información del sistema.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Error inesperado del sistema: ", ex);

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_ERROR",
                "Ha ocurrido un error interno. Por favor, intente nuevamente más tarde.");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
