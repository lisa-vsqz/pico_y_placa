package com.picoyplaca.config;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotEmpty;

/**
 * Configuración externalizada de las reglas de Pico y Placa.
 * <p>
 * Permite cambiar las reglas de restricción (días, dígitos, horarios)
 * sin modificar código fuente, solo editando application.properties
 * o variables de entorno.
 * <p>
 * Principio: Open/Closed — abierto para extensión (nuevas reglas),
 * cerrado para modificación (no se cambia código).
 */
@Configuration
@ConfigurationProperties(prefix = "picoyplaca")
@Validated
public class PicoYPlacaProperties {

    /**
     * Mapa de día de la semana → lista de dígitos restringidos.
     * Ejemplo: MONDAY → [1, 2]
     */
    @NotEmpty(message = "Las reglas de pico y placa deben estar configuradas")
    private Map<DayOfWeek, List<Integer>> rules;

    /**
     * Franjas horarias durante las cuales aplica la restricción.
     */
    @NotEmpty(message = "Los horarios de restricción deben estar configurados")
    private List<TimeSchedule> schedules;

    /**
     * Patrón regex para validar el formato de placa.
     */
    private String platePattern = "^[A-Z]{3}-?\\d{3,4}$";

    // --- Getters y Setters ---

    public Map<DayOfWeek, List<Integer>> getRules() {
        return rules;
    }

    public void setRules(Map<DayOfWeek, List<Integer>> rules) {
        this.rules = rules;
    }

    public List<TimeSchedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<TimeSchedule> schedules) {
        this.schedules = schedules;
    }

    public String getPlatePattern() {
        return platePattern;
    }

    public void setPlatePattern(String platePattern) {
        this.platePattern = platePattern;
    }

    /**
     * Obtiene los dígitos restringidos para un día específico.
     *
     * @param dayOfWeek día de la semana
     * @return conjunto de dígitos restringidos, vacío si no hay restricción
     */
    public Set<Integer> getRestrictedDigits(DayOfWeek dayOfWeek) {
        List<Integer> digits = rules.get(dayOfWeek);
        if (digits == null) {
            return Set.of();
        }
        return Set.copyOf(digits);
    }

    /**
     * Verifica si una hora específica cae dentro de alguna franja de restricción.
     *
     * @param time hora a verificar
     * @return true si la hora está dentro de una franja de restricción
     */
    public boolean isWithinRestrictedHours(LocalTime time) {
        return schedules.stream()
                .anyMatch(schedule -> !time.isBefore(schedule.getStart()) && !time.isAfter(schedule.getEnd()));
    }

    @PostConstruct
    public void validate() {
        // Validar que los dígitos estén entre 0 y 9
        rules.forEach((day, digits) -> {
            digits.forEach(digit -> {
                if (digit < 0 || digit > 9) {
                    throw new IllegalStateException(
                            String.format("Dígito inválido %d para el día %s. Debe estar entre 0 y 9.", digit, day));
                }
            });
        });

        // Validar que los horarios sean coherentes
        schedules.forEach(schedule -> {
            if (schedule.getStart().isAfter(schedule.getEnd())) {
                throw new IllegalStateException(
                        String.format("Horario inválido: inicio %s es posterior a fin %s",
                                schedule.getStart(), schedule.getEnd()));
            }
        });
    }

    /**
     * Representación de una franja horaria de restricción.
     */
    public static class TimeSchedule {
        private LocalTime start;
        private LocalTime end;

        public LocalTime getStart() {
            return start;
        }

        public void setStart(LocalTime start) {
            this.start = start;
        }

        public LocalTime getEnd() {
            return end;
        }

        public void setEnd(LocalTime end) {
            this.end = end;
        }

        @Override
        public String toString() {
            return start + " - " + end;
        }
    }

    /**
     * Retorna una descripción legible de las reglas activas.
     */
    public String describeRules() {
        StringBuilder sb = new StringBuilder("Reglas de Pico y Placa:\n");
        rules.forEach((day, digits) -> {
            String digitsStr = digits.stream().map(String::valueOf).collect(Collectors.joining(", "));
            sb.append(String.format("  %s: dígitos %s%n", day, digitsStr));
        });
        sb.append("Horarios de restricción:\n");
        schedules.forEach(s -> sb.append(String.format("  %s%n", s)));
        return sb.toString();
    }
}
