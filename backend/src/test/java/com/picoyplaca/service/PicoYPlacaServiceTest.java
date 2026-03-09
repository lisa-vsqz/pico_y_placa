package com.picoyplaca.service;

import com.picoyplaca.config.PicoYPlacaProperties;
import com.picoyplaca.domain.dto.PicoYPlacaRequest;
import com.picoyplaca.domain.dto.PicoYPlacaResponse;
import com.picoyplaca.exception.DateInPastException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el servicio de Pico y Placa.
 * <p>
 * Cubre:
 * - Lógica de restricción por día y dígito
 * - Validación de horarios
 * - Casos de borde (inicio/fin de franja, fines de semana)
 * - Validación de fecha en el pasado
 * - Formato de placas
 */
@DisplayName("PicoYPlacaService - Lógica de negocio")
class PicoYPlacaServiceTest {

    private PicoYPlacaService service;
    private PicoYPlacaProperties properties;

    @BeforeEach
    void setUp() {
        properties = new PicoYPlacaProperties();

        // Configurar reglas estándar de Quito
        properties.setRules(Map.of(
                DayOfWeek.MONDAY, List.of(1, 2),
                DayOfWeek.TUESDAY, List.of(3, 4),
                DayOfWeek.WEDNESDAY, List.of(5, 6),
                DayOfWeek.THURSDAY, List.of(7, 8),
                DayOfWeek.FRIDAY, List.of(9, 0)));

        // Configurar horarios de restricción
        PicoYPlacaProperties.TimeSchedule morning = new PicoYPlacaProperties.TimeSchedule();
        morning.setStart(LocalTime.of(7, 0));
        morning.setEnd(LocalTime.of(9, 30));

        PicoYPlacaProperties.TimeSchedule afternoon = new PicoYPlacaProperties.TimeSchedule();
        afternoon.setStart(LocalTime.of(16, 0));
        afternoon.setEnd(LocalTime.of(19, 30));

        properties.setSchedules(List.of(morning, afternoon));
        properties.setPlatePattern("^[A-Z]{3}-?\\d{3,4}$");

        service = new PicoYPlacaService(properties, "America/Guayaquil");
    }

    @Nested
    @DisplayName("Restricción por día y dígito")
    class RestrictionByDayAndDigit {

        @Test
        @DisplayName("Lunes - placa terminada en 1 - en horario restringido → NO puede circular")
        void mondayDigit1RestrictedHours_cannotDrive() {
            // Lunes a las 08:00 (dentro de franja matutina)
            LocalDateTime monday8am = LocalDateTime.of(2026, 3, 9, 8, 0);
            assertFalse(service.canVehicleDrive(monday8am, 1));
        }

        @Test
        @DisplayName("Lunes - placa terminada en 2 - en horario restringido → NO puede circular")
        void mondayDigit2RestrictedHours_cannotDrive() {
            LocalDateTime monday17pm = LocalDateTime.of(2026, 3, 9, 17, 0);
            assertFalse(service.canVehicleDrive(monday17pm, 2));
        }

        @Test
        @DisplayName("Lunes - placa terminada en 3 (no restringida) → PUEDE circular")
        void mondayDigit3_canDrive() {
            LocalDateTime monday8am = LocalDateTime.of(2026, 3, 9, 8, 0);
            assertTrue(service.canVehicleDrive(monday8am, 3));
        }

        @ParameterizedTest(name = "Martes - dígito {0} - restringido")
        @ValueSource(ints = { 3, 4 })
        @DisplayName("Martes - dígitos 3 y 4 restringidos en horario")
        void tuesdayRestrictedDigits(int digit) {
            LocalDateTime tuesday8am = LocalDateTime.of(2026, 3, 10, 8, 0);
            assertFalse(service.canVehicleDrive(tuesday8am, digit));
        }

        @ParameterizedTest(name = "Miércoles - dígito {0} - restringido")
        @ValueSource(ints = { 5, 6 })
        @DisplayName("Miércoles - dígitos 5 y 6 restringidos en horario")
        void wednesdayRestrictedDigits(int digit) {
            LocalDateTime wednesday17pm = LocalDateTime.of(2026, 3, 11, 17, 0);
            assertFalse(service.canVehicleDrive(wednesday17pm, digit));
        }

        @ParameterizedTest(name = "Jueves - dígito {0} - restringido")
        @ValueSource(ints = { 7, 8 })
        @DisplayName("Jueves - dígitos 7 y 8 restringidos en horario")
        void thursdayRestrictedDigits(int digit) {
            LocalDateTime thursday9am = LocalDateTime.of(2026, 3, 12, 9, 0);
            assertFalse(service.canVehicleDrive(thursday9am, digit));
        }

        @ParameterizedTest(name = "Viernes - dígito {0} - restringido")
        @ValueSource(ints = { 9, 0 })
        @DisplayName("Viernes - dígitos 9 y 0 restringidos en horario")
        void fridayRestrictedDigits(int digit) {
            LocalDateTime friday19pm = LocalDateTime.of(2026, 3, 13, 19, 0);
            assertFalse(service.canVehicleDrive(friday19pm, digit));
        }
    }

    @Nested
    @DisplayName("Restricción por horario")
    class RestrictionBySchedule {

        @Test
        @DisplayName("Antes de la franja matutina (06:59) → PUEDE circular")
        void beforeMorningSchedule_canDrive() {
            LocalDateTime monday659 = LocalDateTime.of(2026, 3, 9, 6, 59);
            assertTrue(service.canVehicleDrive(monday659, 1));
        }

        @Test
        @DisplayName("Inicio exacto de franja matutina (07:00) → NO puede circular")
        void exactStartMorningSchedule_cannotDrive() {
            LocalDateTime monday700 = LocalDateTime.of(2026, 3, 9, 7, 0);
            assertFalse(service.canVehicleDrive(monday700, 1));
        }

        @Test
        @DisplayName("Fin exacto de franja matutina (09:30) → NO puede circular")
        void exactEndMorningSchedule_cannotDrive() {
            LocalDateTime monday930 = LocalDateTime.of(2026, 3, 9, 9, 30);
            assertFalse(service.canVehicleDrive(monday930, 1));
        }

        @Test
        @DisplayName("Después de la franja matutina (09:31) → PUEDE circular")
        void afterMorningSchedule_canDrive() {
            LocalDateTime monday931 = LocalDateTime.of(2026, 3, 9, 9, 31);
            assertTrue(service.canVehicleDrive(monday931, 1));
        }

        @Test
        @DisplayName("Entre franjas (12:00) → PUEDE circular")
        void betweenSchedules_canDrive() {
            LocalDateTime monday12pm = LocalDateTime.of(2026, 3, 9, 12, 0);
            assertTrue(service.canVehicleDrive(monday12pm, 1));
        }

        @Test
        @DisplayName("Inicio de franja vespertina (16:00) → NO puede circular")
        void startAfternoonSchedule_cannotDrive() {
            LocalDateTime monday16pm = LocalDateTime.of(2026, 3, 9, 16, 0);
            assertFalse(service.canVehicleDrive(monday16pm, 1));
        }

        @Test
        @DisplayName("Fin de franja vespertina (19:30) → NO puede circular")
        void endAfternoonSchedule_cannotDrive() {
            LocalDateTime monday1930 = LocalDateTime.of(2026, 3, 9, 19, 30);
            assertFalse(service.canVehicleDrive(monday1930, 1));
        }

        @Test
        @DisplayName("Después de franja vespertina (19:31) → PUEDE circular")
        void afterAfternoonSchedule_canDrive() {
            LocalDateTime monday1931 = LocalDateTime.of(2026, 3, 9, 19, 31);
            assertTrue(service.canVehicleDrive(monday1931, 1));
        }
    }

    @Nested
    @DisplayName("Fines de semana")
    class Weekends {

        @ParameterizedTest(name = "Sábado - dígito {0} - puede circular")
        @ValueSource(ints = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 })
        @DisplayName("Sábado - todos los dígitos pueden circular")
        void saturdayAllDigits_canDrive(int digit) {
            LocalDateTime saturday8am = LocalDateTime.of(2026, 3, 14, 8, 0);
            assertTrue(service.canVehicleDrive(saturday8am, digit));
        }

        @ParameterizedTest(name = "Domingo - dígito {0} - puede circular")
        @ValueSource(ints = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 })
        @DisplayName("Domingo - todos los dígitos pueden circular")
        void sundayAllDigits_canDrive(int digit) {
            LocalDateTime sunday17pm = LocalDateTime.of(2026, 3, 15, 17, 0);
            assertTrue(service.canVehicleDrive(sunday17pm, digit));
        }
    }

    @Nested
    @DisplayName("Validación de fecha")
    class DateValidation {

        @Test
        @DisplayName("Fecha en el pasado → lanza DateInPastException")
        void pastDate_throwsException() {
            PicoYPlacaRequest request = new PicoYPlacaRequest(
                    "PBX-1234",
                    LocalDateTime.of(2020, 1, 1, 8, 0));

            assertThrows(DateInPastException.class,
                    () -> service.checkRestriction(request));
        }
    }

    @Nested
    @DisplayName("Integración del servicio completo")
    class FullServiceIntegration {

        @Test
        @DisplayName("Consulta exitosa - vehículo puede circular")
        void successfulQuery_canDrive() {
            // Un viernes futuro lejano, a las 12:00, placa terminada en 1
            PicoYPlacaRequest request = new PicoYPlacaRequest(
                    "PBX-1231",
                    LocalDateTime.of(2030, 3, 15, 12, 0)); // viernes

            PicoYPlacaResponse response = service.checkRestriction(request);

            assertTrue(response.canDrive());
            assertEquals("PBX-1231", response.plateNumber());
            assertNotNull(response.message());
            assertTrue(response.message().contains("PUEDE circular"));
        }

        @Test
        @DisplayName("Consulta exitosa - vehículo NO puede circular")
        void successfulQuery_cannotDrive() {
            // Un viernes futuro, a las 08:00, placa terminada en 9
            PicoYPlacaRequest request = new PicoYPlacaRequest(
                    "PBX-1239",
                    LocalDateTime.of(2030, 3, 15, 8, 0)); // viernes

            PicoYPlacaResponse response = service.checkRestriction(request);

            assertFalse(response.canDrive());
            assertEquals("PBX-1239", response.plateNumber());
            assertTrue(response.message().contains("NO puede circular"));
        }
    }

    @Nested
    @DisplayName("Formato de placa")
    class PlateFormat {

        @Test
        @DisplayName("Placa con guión se normaliza correctamente")
        void plateWithDash_normalized() {
            PicoYPlacaRequest request = new PicoYPlacaRequest("pbx-1234", LocalDateTime.now());
            assertEquals("PBX-1234", request.normalizedPlate());
        }

        @Test
        @DisplayName("Placa sin guión se normaliza añadiendo guión")
        void plateWithoutDash_normalized() {
            PicoYPlacaRequest request = new PicoYPlacaRequest("PBX1234", LocalDateTime.now());
            assertEquals("PBX-1234", request.normalizedPlate());
        }

        @Test
        @DisplayName("Último dígito extraído correctamente")
        void lastDigitExtracted() {
            assertEquals(4, new PicoYPlacaRequest("PBX-1234", LocalDateTime.now()).lastDigit());
            assertEquals(0, new PicoYPlacaRequest("ABC-1230", LocalDateTime.now()).lastDigit());
            assertEquals(9, new PicoYPlacaRequest("XYZ-999", LocalDateTime.now()).lastDigit());
        }

        @ParameterizedTest(name = "Placa {0} → último dígito {1}")
        @CsvSource({
                "PBX-1234, 4",
                "ABC-1230, 0",
                "XYZ-999, 9",
                "PBX1231, 1",
                "abc-5678, 8"
        })
        @DisplayName("Extracción de último dígito para diferentes formatos")
        void lastDigitVariousFormats(String plate, int expectedDigit) {
            PicoYPlacaRequest request = new PicoYPlacaRequest(plate, LocalDateTime.now());
            assertEquals(expectedDigit, request.lastDigit());
        }
    }
}
