package com.safetrip.backend.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAnalyticsResponse {
    private Long totalUsers;
    private Long activeUsers; // Con al menos una póliza
    private Long newUsersThisMonth;
    private List<TopCustomerDTO> topCustomers;
    private UserGrowthDTO userGrowth;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCustomerDTO {
        private Long userId;
        private String fullName;
        private String email;
        private Long totalPolicies;
        private BigDecimal totalSpent;
        private ZonedDateTime lastPurchase;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserGrowthDTO {
        private Long last7Days;
        private Long last30Days;
        private Long last90Days;
    }
}