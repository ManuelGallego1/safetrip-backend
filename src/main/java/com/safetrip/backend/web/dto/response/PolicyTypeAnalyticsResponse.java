package com.safetrip.backend.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyTypeAnalyticsResponse {
    private Long policyTypeId;
    private String policyTypeName;
    private Long totalSold;
    private BigDecimal totalRevenue;
    private Long totalPersonsInsured;
    private BigDecimal averagePrice;
    private Double conversionRate; // Si tienes datos de abandonos
    private List<MonthlyTrendDTO> monthlyTrend;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTrendDTO {
        private String month;
        private Long count;
        private BigDecimal revenue;
    }
}