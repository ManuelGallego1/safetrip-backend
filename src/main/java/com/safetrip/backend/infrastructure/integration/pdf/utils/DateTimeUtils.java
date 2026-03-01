package com.safetrip.backend.infrastructure.integration.pdf.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Utilidad para manejar conversiones de fecha/hora a zona horaria de Colombia
 */
@Slf4j
public class DateTimeUtils {

    // Zona horaria de Colombia (UTC-5)
    public static final ZoneId COLOMBIA_ZONE = ZoneId.of("America/Bogota");

    // Formateadores
    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * 🆕 Crea un ZonedDateTime en zona horaria Colombia desde una fecha local
     * @param date Fecha local
     * @param hour Hora (0-23)
     * @param minute Minuto (0-59)
     * @return ZonedDateTime en zona America/Bogota
     */
    public static ZonedDateTime createColombiaDateTime(LocalDate date, int hour, int minute) {
        return ZonedDateTime.of(
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth(),
                hour,
                minute,
                0,
                0,
                COLOMBIA_ZONE
        );
    }

    /**
     * Convierte un Instant a zona horaria de Colombia
     */
    public static ZonedDateTime toColombiaTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(COLOMBIA_ZONE);
    }

    /**
     * Convierte un LocalDateTime (sin zona horaria) a Colombia
     */
    public static ZonedDateTime toColombiaTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        // Asume que el LocalDateTime ya está en Colombia
        return localDateTime.atZone(COLOMBIA_ZONE);
    }

    /**
     * Convierte un ZonedDateTime de cualquier zona horaria a Colombia
     */
    public static ZonedDateTime toColombiaZone(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }
        return zonedDateTime.withZoneSameInstant(COLOMBIA_ZONE);
    }

    /**
     * Obtiene la fecha/hora actual en Colombia
     */
    public static ZonedDateTime nowInColombia() {
        return ZonedDateTime.now(COLOMBIA_ZONE);
    }

    /**
     * Extrae el día (dd) de una fecha en zona horaria Colombia
     */
    public static String getDayInColombia(Instant instant) {
        if (instant == null) {
            return null;
        }
        ZonedDateTime colombiaTime = toColombiaTime(instant);
        return String.format("%02d", colombiaTime.getDayOfMonth());
    }

    /**
     * Extrae el mes (MM) de una fecha en zona horaria Colombia
     */
    public static String getMonthInColombia(Instant instant) {
        if (instant == null) {
            return null;
        }
        ZonedDateTime colombiaTime = toColombiaTime(instant);
        return String.format("%02d", colombiaTime.getMonthValue());
    }

    /**
     * Extrae el año (yyyy) de una fecha en zona horaria Colombia
     */
    public static String getYearInColombia(Instant instant) {
        if (instant == null) {
            return null;
        }
        ZonedDateTime colombiaTime = toColombiaTime(instant);
        return String.valueOf(colombiaTime.getYear());
    }

    /**
     * Extrae la hora (HH) de una fecha en zona horaria Colombia
     */
    public static String getHourInColombia(Instant instant) {
        if (instant == null) {
            return null;
        }
        ZonedDateTime colombiaTime = toColombiaTime(instant);
        return String.valueOf(colombiaTime.getHour());
    }

    /**
     * Formatea hora en formato HH:mm para Colombia
     */
    public static String formatHourInColombia(Instant instant) {
        if (instant == null) {
            return null;
        }
        ZonedDateTime colombiaTime = toColombiaTime(instant);
        return colombiaTime.format(HOUR_FORMATTER);
    }

    /**
     * Formatea fecha completa en formato dd/MM/yyyy para Colombia
     */
    public static String formatDateInColombia(Instant instant) {
        if (instant == null) {
            return null;
        }
        ZonedDateTime colombiaTime = toColombiaTime(instant);
        return colombiaTime.format(DATE_FORMATTER);
    }

    /**
     * Formatea fecha y hora completa en formato dd/MM/yyyy HH:mm para Colombia
     */
    public static String formatDateTimeInColombia(Instant instant) {
        if (instant == null) {
            return null;
        }
        ZonedDateTime colombiaTime = toColombiaTime(instant);
        return colombiaTime.format(DATETIME_FORMATTER);
    }

    /**
     * Convierte un String de hora (ej: "9" o "15") a formato HH:00 en Colombia
     */
    public static String formatHourString(String hour) {
        if (hour == null || hour.trim().isEmpty()) {
            return null;
        }
        try {
            int hourInt = Integer.parseInt(hour.trim());
            return String.format("%02d:00", hourInt);
        } catch (NumberFormatException e) {
            log.warn("Invalid hour format: {}", hour);
            return hour;
        }
    }

    /**
     * Convierte LocalDateTime a los componentes de fecha/hora en Colombia
     */
    public static DateTimeComponents toComponents(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        ZonedDateTime colombiaTime = toColombiaTime(dateTime);
        return new DateTimeComponents(colombiaTime);
    }

    /**
     * Convierte Instant a los componentes de fecha/hora en Colombia
     */
    public static DateTimeComponents toComponents(Instant instant) {
        if (instant == null) {
            return null;
        }
        ZonedDateTime colombiaTime = toColombiaTime(instant);
        return new DateTimeComponents(colombiaTime);
    }

    /**
     * 🆕 Convierte ZonedDateTime a los componentes de fecha/hora en Colombia
     * Primero convierte la zona horaria a Colombia, luego extrae los componentes
     */
    public static DateTimeComponents toComponents(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }
        // Convertir a zona horaria de Colombia si no lo está ya
        ZonedDateTime colombiaTime = toColombiaZone(zonedDateTime);
        return new DateTimeComponents(colombiaTime);
    }

    /**
     * Clase auxiliar para contener componentes de fecha/hora
     */
    public static class DateTimeComponents {
        private final String day;
        private final String month;
        private final String year;
        private final String hour;
        private final String hourFormatted;

        public DateTimeComponents(ZonedDateTime dateTime) {
            this.day = String.format("%02d", dateTime.getDayOfMonth());
            this.month = String.format("%02d", dateTime.getMonthValue());
            this.year = String.valueOf(dateTime.getYear());
            this.hour = String.valueOf(dateTime.getHour());
            this.hourFormatted = dateTime.format(HOUR_FORMATTER);
        }

        public String getDay() { return day; }
        public String getMonth() { return month; }
        public String getYear() { return year; }
        public String getHour() { return hour; }
        public String getHourFormatted() { return hourFormatted; }
    }
}