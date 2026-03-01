package com.safetrip.backend.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminSalesKPIResponse {

    // Totales generales
    private BigDecimal totalRevenue;
    private Long totalPoliciesSold;
    private Long totalPersonsInsured;
    private Long totalCustomers;
    private Long totalPaxSold; // NUEVO: Total de PAX vendidos
    private Long approximatePax;

    // Contadores por estado
    private Long completedPolicies;
    private Long pendingPolicies;
    private Long failedPolicies;

    // Promedios
    private BigDecimal averageOrderValue;
    private Double averagePersonsPerPolicy;

    // Ventas por tipo de póliza
    private List<PolicyTypeSalesDTO> salesByPolicyType;

    // Ventas por método de pago
    private List<PaymentMethodSalesDTO> salesByPaymentMethod;

    // Ingresos por periodo
    private RevenueByPeriodDTO revenueByPeriod;

    // Estadísticas de descuentos
    private DiscountStatsDTO discountStats;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PolicyTypeSalesDTO {
        private String policyTypeName;
        private Long count;
        private BigDecimal revenue;
        private Double percentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentMethodSalesDTO {
        private String paymentMethod;
        private Long count;
        private BigDecimal revenue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueByPeriodDTO {
        private BigDecimal last7Days;
        private BigDecimal last30Days;
        private BigDecimal last90Days;
        private BigDecimal allTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiscountStatsDTO {
        private Long totalPoliciesWithDiscount;
        private BigDecimal totalDiscountAmount;
        private List<DiscountUsageDTO> topDiscounts;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiscountUsageDTO {
        private String discountCode;
        private Long usageCount;
        private BigDecimal totalSaved;
    }
}