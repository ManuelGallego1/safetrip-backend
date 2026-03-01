package com.safetrip.backend.application.service.impl;

import com.safetrip.backend.application.service.DiscountService;
import com.safetrip.backend.domain.exception.DiscountNotFoundException;
import com.safetrip.backend.domain.model.Discount;
import com.safetrip.backend.domain.repository.DiscountRepository;
import com.safetrip.backend.domain.service.DiscountValidator;
import com.safetrip.backend.web.dto.request.DiscountRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;
    private final DiscountValidator discountValidator;

    public DiscountServiceImpl(DiscountRepository discountRepository,
                               DiscountValidator discountValidator) {
        this.discountRepository = discountRepository;
        this.discountValidator = discountValidator;
    }

    @Override
    @Transactional
    public Discount createDiscount(DiscountRequest request) {
        // Validar nombre
        discountValidator.validateName(request.getName());

        // Validar unicidad del nombre
        discountValidator.validateNameUniqueness(request.getName());

        // Validar valor según el tipo
        discountValidator.validateDiscountValue(request.getType(), request.getValue());

        // Crear el descuento
        Discount discount = Discount.create(
                request.getName(),
                request.getType(),
                request.getValue()
        );

        // Si se especifica active=false, actualizar
        if (request.getActive() != null && !request.getActive()) {
            discount = discount.updateActive(false);
        }

        // Guardar y retornar
        return discountRepository.save(discount);
    }

    @Override
    @Transactional(readOnly = true)
    public Discount findByName(String name) {
        return discountRepository.findByName(name)
                .orElseThrow(() -> new DiscountNotFoundException(
                        "No se encontró un descuento con el nombre: " + name
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Discount findById(Long id) {
        return discountRepository.findById(id)
                .orElseThrow(() -> new DiscountNotFoundException(
                        "No se encontró un descuento con el ID: " + id
                ));
    }

    @Override
    @Transactional
    public Discount updateDiscount(Long id, DiscountRequest request) {
        // Buscar el descuento existente
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new DiscountNotFoundException(
                        "No se encontró un descuento con el ID: " + id
                ));

        boolean changed = false;

        // Actualizar nombre si es diferente
        if (request.getName() != null && !request.getName().isBlank()
                && !request.getName().equals(discount.getName())) {
            discountValidator.validateName(request.getName());
            discountValidator.validateNameUniqueness(request.getName(), id);
            discount = discount.updateName(request.getName());
            changed = true;
        }

        // Actualizar tipo si es diferente
        if (request.getType() != null && !request.getType().equals(discount.getType())) {
            discount = discount.updateType(request.getType());
            changed = true;
        }

        // Actualizar valor si es diferente
        if (request.getValue() != null && request.getValue().compareTo(discount.getValue()) != 0) {
            discountValidator.validateDiscountValue(
                    request.getType() != null ? request.getType() : discount.getType(),
                    request.getValue()
            );
            discount = discount.updateValue(request.getValue());
            changed = true;
        }

        // Actualizar estado activo si es diferente
        if (request.getActive() != null && !request.getActive().equals(discount.getActive())) {
            discount = discount.updateActive(request.getActive());
            changed = true;
        }

        // Guardar solo si hubo cambios
        if (changed) {
            return discountRepository.save(discount);
        }

        return discount;
    }

    @Override
    @Transactional
    public void deleteDiscount(Long id) {
        // Verificar que existe
        if (!discountRepository.findById(id).isPresent()) {
            throw new DiscountNotFoundException(
                    "No se encontró un descuento con el ID: " + id
            );
        }

        discountRepository.deleteById(id);
    }
}