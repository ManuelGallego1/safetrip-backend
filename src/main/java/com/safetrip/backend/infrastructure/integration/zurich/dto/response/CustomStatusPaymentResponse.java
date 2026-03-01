package com.safetrip.backend.infrastructure.integration.zurich.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomStatusPaymentResponse {

    private String voucher;

    @JsonProperty("payment_status")
    private String paymentStatus;

    @JsonProperty("payment_description")
    private String paymentDescription;

    @JsonProperty("general_info")
    private GeneralInfo generalInfo;

    @Data
    public static class GeneralInfo {

        private String nit;

        @JsonProperty("end_date")
        private LocalDate endDate;

        @JsonProperty("plan_name")
        private String planName;

        @JsonProperty("hotel_name")
        private String hotelName;

        @JsonProperty("start_date")
        private LocalDate startDate;
    }
}