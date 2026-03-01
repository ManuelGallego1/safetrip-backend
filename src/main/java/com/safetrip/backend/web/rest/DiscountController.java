package com.safetrip.backend.web.rest;

import com.safetrip.backend.application.service.DiscountService;
import com.safetrip.backend.domain.model.Discount;
import com.safetrip.backend.web.dto.request.DiscountRequest;
import com.safetrip.backend.web.dto.response.ApiResponse;
import com.safetrip.backend.web.dto.response.DiscountResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/discounts")
@SecurityRequirement(name = "bearerAuth")
public class DiscountController {

    private final DiscountService discountService;

    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }

    /**
     * POST /api/discounts - Crear un nuevo descuento
     */
    @PostMapping
    public ResponseEntity<ApiResponse<DiscountResponse>> createDiscount(
            @Valid @RequestBody DiscountRequest request) {

        Discount discount = discountService.createDiscount(request);
        DiscountResponse response = DiscountResponse.fromDomain(discount);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Descuento creado exitosamente", response));
    }

    /**
     * GET /api/discounts/name/{name} - Buscar descuento por nombre
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<DiscountResponse>> getDiscountByName(
            @PathVariable String name) {

        Discount discount = discountService.findByName(name);
        DiscountResponse response = DiscountResponse.fromDomain(discount);

        return ResponseEntity.ok(
                ApiResponse.success("Descuento encontrado exitosamente", response)
        );
    }

    /**
     * GET /api/discounts/{id} - Buscar descuento por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DiscountResponse>> getDiscountById(
            @PathVariable Long id) {

        Discount discount = discountService.findById(id);
        DiscountResponse response = DiscountResponse.fromDomain(discount);

        return ResponseEntity.ok(
                ApiResponse.success("Descuento encontrado exitosamente", response)
        );
    }

    /**
     * PUT /api/discounts/{id} - Actualizar un descuento
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DiscountResponse>> updateDiscount(
            @PathVariable Long id,
            @Valid @RequestBody DiscountRequest request) {

        Discount discount = discountService.updateDiscount(id, request);
        DiscountResponse response = DiscountResponse.fromDomain(discount);

        return ResponseEntity.ok(
                ApiResponse.success("Descuento actualizado exitosamente", response)
        );
    }

    /**
     * DELETE /api/discounts/{id} - Eliminar un descuento
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDiscount(@PathVariable Long id) {
        discountService.deleteDiscount(id);

        return ResponseEntity.ok(
                ApiResponse.success("Descuento eliminado exitosamente", null)
        );
    }
}