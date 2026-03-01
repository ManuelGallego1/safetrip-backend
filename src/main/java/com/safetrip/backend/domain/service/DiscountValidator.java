package com.safetrip.backend.domain.service;

import com.safetrip.backend.domain.exception.DuplicateDiscountNameException;
import com.safetrip.backend.domain.exception.InvalidDiscountException;
import com.safetrip.backend.domain.model.enums.DiscountType;
import com.safetrip.backend.domain.repository.DiscountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DiscountValidator {

    private final DiscountRepository discountRepository;

    public DiscountValidator(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    /**
     * Valida que el nombre del descuento sea único
     */
    public void validateNameUniqueness(String name) {
        if (discountRepository.existsByName(name)) {
            throw new DuplicateDiscountNameException(
                    "Ya existe un descuento con el nombre: " + name
            );
        }
    }

    /**
     * Valida que el nombre del descuento sea único excluyendo un ID específico
     */
    public void validateNameUniqueness(String name, Long excludeDiscountId) {
        if (discountRepository.existsByNameAndDiscountIdNot(name, excludeDiscountId)) {
            throw new DuplicateDiscountNameException(
                    "Ya existe un descuento con el nombre: " + name
            );
        }
    }

    /**
     * Valida los valores del descuento según su tipo
     */
    public void validateDiscountValue(DiscountType type, BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidDiscountException("El valor del descuento debe ser mayor o igual a 0");
        }

        if (type == DiscountType.PERCENTAGE) {
            if (value.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new InvalidDiscountException(
                        "El porcentaje de descuento no puede ser mayor a 100"
                );
            }
            if (value.compareTo(BigDecimal.ZERO) == 0) {
                throw new InvalidDiscountException(
                        "El porcentaje de descuento debe ser mayor a 0"
                );
            }
        }

        if (type == DiscountType.FIXED) {
            if (value.compareTo(BigDecimal.ZERO) == 0) {
                throw new InvalidDiscountException(
                        "El descuento fijo debe ser mayor a 0"
                );
            }
        }

        if (type == DiscountType.ADVISOR) {
            if (value.compareTo(BigDecimal.ZERO) == 0) {
                throw new InvalidDiscountException(
                        "El valor del descuento debe ser mayor o igual a 0"
                );
            }
        }

    }

    /**
     * Valida que el nombre no esté vacío
     */
    public void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidDiscountException("El nombre del descuento es obligatorio");
        }
        if (name.length() > 100) {
            throw new InvalidDiscountException(
                    "El nombre del descuento no puede exceder 100 caracteres"
            );
        }
    }
}