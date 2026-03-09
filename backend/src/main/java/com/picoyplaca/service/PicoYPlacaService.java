package com.picoyplaca.service;

import com.picoyplaca.config.PicoYPlacaProperties;
import com.picoyplaca.domain.dto.PicoYPlacaRequest;
import com.picoyplaca.domain.dto.PicoYPlacaResponse;
import com.picoyplaca.exception.DateInPastException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Set;

/**
 * Servicio que implementa la lógica de negocio de Pico y Placa.
 * <p>
 * Responsabilidad única: determinar si un vehículo puede circular
 * en una fecha y hora dada, basándose en las reglas configuradas.
 * <p>
 * Las reglas son inyectadas desde la configuración, haciendo este
 * servicio independiente de reglas específicas (principio OCP).
 */
@Service
public class PicoYPlacaService {

    private static final Logger log = LoggerFactory.getLogger(PicoYPlacaService.class);

    private final PicoYPlacaProperties properties;
    private final ZoneId applicationTimezone;

    public PicoYPlacaService(
            PicoYPlacaProperties properties,
            @Value("${app.timezone:America/Guayaquil}") String timezone) {
        this.properties = properties;
        this.applicationTimezone = ZoneId.of(timezone);
        log.info("Servicio Pico y Placa inicializado con zona horaria: {}", timezone);
        log.info(properties.describeRules());
    }

    /**
     * Verifica si un vehículo puede circular según las reglas de Pico y Placa.
     *
     * @param request datos de la consulta (placa, fecha y hora)
     * @return respuesta indicando si puede circular y mensaje descriptivo
     * @throws DateInPastException si la fecha proporcionada es anterior a la actual
     */
    public PicoYPlacaResponse checkRestriction(PicoYPlacaRequest request) {
        String normalizedPlate = request.normalizedPlate();
        LocalDateTime dateTime = request.dateTime();
        int lastDigit = request.lastDigit();

        log.info("Consultando restricción para placa que termina en {} para fecha {}",
                lastDigit, dateTime);

        // Validar que la fecha no sea anterior a la actual
        validateDateNotInPast(dateTime);

        // Determinar si puede circular
        boolean canDrive = canVehicleDrive(dateTime, lastDigit);

        // Construir mensaje descriptivo
        String message = buildResponseMessage(normalizedPlate, dateTime, canDrive);

        log.info("Resultado para placa terminada en {}: {}",
                lastDigit, canDrive ? "PUEDE circular" : "NO puede circular");

        return new PicoYPlacaResponse(normalizedPlate, dateTime, canDrive, message);
    }

    /**
     * Lógica central: determina si un vehículo puede circular.
     * <p>
     * Un vehículo NO puede circular si se cumplen TODAS las condiciones:
     * 1. El día de la semana tiene restricción para su último dígito
     * 2. La hora cae dentro de una franja horaria de restricción
     *
     * @param dateTime  fecha y hora de consulta
     * @param lastDigit último dígito de la placa
     * @return true si el vehículo puede circular
     */
    boolean canVehicleDrive(LocalDateTime dateTime, int lastDigit) {
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        LocalTime time = dateTime.toLocalTime();

        // Verificar si el dígito está restringido para este día
        Set<Integer> restrictedDigits = properties.getRestrictedDigits(dayOfWeek);
        boolean isDigitRestricted = restrictedDigits.contains(lastDigit);

        if (!isDigitRestricted) {
            log.debug("Dígito {} no está restringido el {}", lastDigit, dayOfWeek);
            return true;
        }

        // Verificar si la hora cae dentro de las franjas de restricción
        boolean isWithinRestrictedHours = properties.isWithinRestrictedHours(time);

        if (!isWithinRestrictedHours) {
            log.debug("Hora {} está fuera de franjas de restricción", time);
            return true;
        }

        // El vehículo está restringido
        log.debug("Vehículo con dígito {} está restringido el {} a las {}", lastDigit, dayOfWeek, time);
        return false;
    }

    /**
     * Valida que la fecha no sea anterior a la fecha/hora actual del sistema.
     */
    private void validateDateNotInPast(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now(applicationTimezone);
        if (dateTime.isBefore(now)) {
            throw new DateInPastException(
                    String.format("La fecha y hora %s es anterior a la fecha y hora actual %s. " +
                            "Solo se permiten consultas para el momento actual o futuro.", dateTime, now));
        }
    }

    /**
     * Construye un mensaje descriptivo del resultado de la consulta.
     */
    private String buildResponseMessage(String plate, LocalDateTime dateTime, boolean canDrive) {
        String dayName = dateTime.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "EC"));
        String formattedDate = String.format("%02d/%02d/%04d",
                dateTime.getDayOfMonth(), dateTime.getMonthValue(), dateTime.getYear());
        String formattedTime = String.format("%02d:%02d",
                dateTime.getHour(), dateTime.getMinute());

        if (canDrive) {
            return String.format(
                    "El vehículo con placa %s PUEDE circular el %s %s a las %s.",
                    plate, dayName, formattedDate, formattedTime);
        } else {
            return String.format(
                    "El vehículo con placa %s NO puede circular el %s %s a las %s " +
                            "debido a la restricción de Pico y Placa.",
                    plate, dayName, formattedDate, formattedTime);
        }
    }
}
