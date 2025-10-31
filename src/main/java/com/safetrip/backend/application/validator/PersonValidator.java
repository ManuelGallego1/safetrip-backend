package com.safetrip.backend.application.validator;

import java.util.regex.Pattern;

public class PersonValidator {

    private static final Pattern DOUBLE_SPACE_PATTERN = Pattern.compile("\\s{2,}");
    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$");

    /**
     * Valida y normaliza un nombre completo
     * Debe tener al menos 2 palabras (nombre y apellido)
     * Elimina espacios dobles
     *
     * @param fullName nombre completo a validar
     * @return nombre normalizado
     * @throws IllegalArgumentException si el nombre no es válido
     */
    public static String validateAndNormalizeFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre completo no puede estar vacío");
        }

        String normalized = fullName.trim();
        normalized = DOUBLE_SPACE_PATTERN.matcher(normalized).replaceAll(" ");

        if (!VALID_NAME_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                    "El nombre solo puede contener letras y espacios: " + fullName
            );
        }

        String[] parts = normalized.split("\\s+");
        if (parts.length < 2) {
            throw new IllegalArgumentException(
                    "El nombre debe tener al menos un nombre y un apellido. Recibido: " + fullName
            );
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) result.append(" ");
            result.append(capitalize(parts[i]));
        }

        return result.toString();
    }

    /**
     * Capitaliza la primera letra de una palabra
     */
    private static String capitalize(String word) {
        if (word == null || word.isEmpty()) return word;
        return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
    }

    /**
     * Valida un número de documento
     */
    public static String validateDocumentNumber(String documentNumber) {
        if (documentNumber == null || documentNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de documento no puede estar vacío");
        }

        String normalized = documentNumber.trim();

        if (normalized.length() < 3) {
            throw new IllegalArgumentException(
                    "El número de documento es demasiado corto: " + documentNumber
            );
        }

        return normalized;
    }
}