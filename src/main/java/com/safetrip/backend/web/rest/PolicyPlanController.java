package com.safetrip.backend.web.rest;

import com.safetrip.backend.application.service.PolicyPlanService;
import com.safetrip.backend.web.dto.response.ApiResponse;
import com.safetrip.backend.web.dto.response.PolicyPlanResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/policy-plans")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Permite CORS
@SecurityRequirement(name = "bearerAuth")
public class PolicyPlanController {

    private final PolicyPlanService policyPlanService;

    /**
     * Obtiene todos los planes activos
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PolicyPlanResponse>>> getAllActivePlans() {
        log.info("📋 Obteniendo planes de póliza activos");

        try {
            List<PolicyPlanResponse> plans = policyPlanService.getAllActivePlans();
            log.info("✅ {} planes encontrados", plans.size());

            return ResponseEntity.ok(
                    ApiResponse.success("Planes obtenidos exitosamente", plans)
            );
        } catch (Exception e) {
            log.error("❌ Error obteniendo planes: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error al obtener planes", null));
        }
    }

    /**
     * Obtiene planes de PAX (policyTypeId = 1)
     */
    @GetMapping("/pax")
    public ResponseEntity<ApiResponse<List<PolicyPlanResponse>>> getPaxPlans() {
        log.info("👥 Obteniendo planes de PAX (policyTypeId = 1)");

        try {
            List<PolicyPlanResponse> plans = policyPlanService.getActivePaxPlans();
            log.info("✅ {} planes de PAX encontrados", plans.size());

            return ResponseEntity.ok(
                    ApiResponse.success("Planes de PAX obtenidos exitosamente", plans)
            );
        } catch (Exception e) {
            log.error("❌ Error obteniendo planes de PAX: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error al obtener planes de PAX", null));
        }
    }

    /**
     * Obtiene planes de TIEMPO (policyTypeId = 2)
     */
    @GetMapping("/time")
    public ResponseEntity<ApiResponse<List<PolicyPlanResponse>>> getTimePlans() {
        log.info("⏰ Obteniendo planes de TIEMPO (policyTypeId = 2)");

        try {
            List<PolicyPlanResponse> plans = policyPlanService.getActiveTimePlans();
            log.info("✅ {} planes de TIEMPO encontrados", plans.size());

            return ResponseEntity.ok(
                    ApiResponse.success("Planes de TIEMPO obtenidos exitosamente", plans)
            );
        } catch (Exception e) {
            log.error("❌ Error obteniendo planes de TIEMPO: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error al obtener planes de TIEMPO", null));
        }
    }

    /**
     * Obtiene planes por tipo de póliza (genérico)
     */
    @GetMapping("/policy-type/{policyTypeId}")
    public ResponseEntity<ApiResponse<List<PolicyPlanResponse>>> getPlansByPolicyType(
            @PathVariable Long policyTypeId) {

        log.info("📋 Obteniendo planes para tipo de póliza: {}", policyTypeId);

        try {
            List<PolicyPlanResponse> plans = policyPlanService
                    .getPlansByPolicyType(policyTypeId);

            log.info("✅ {} planes encontrados para tipo {}", plans.size(), policyTypeId);

            return ResponseEntity.ok(
                    ApiResponse.success("Planes obtenidos exitosamente", plans)
            );
        } catch (Exception e) {
            log.error("❌ Error obteniendo planes: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error al obtener planes", null));
        }
    }

    /**
     * Obtiene solo los planes populares
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<PolicyPlanResponse>>> getPopularPlans() {
        log.info("⭐ Obteniendo planes populares");

        try {
            List<PolicyPlanResponse> plans = policyPlanService.getPopularPlans();
            log.info("✅ {} planes populares encontrados", plans.size());

            return ResponseEntity.ok(
                    ApiResponse.success("Planes populares obtenidos exitosamente", plans)
            );
        } catch (Exception e) {
            log.error("❌ Error obteniendo planes populares: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error al obtener planes populares", null));
        }
    }
}