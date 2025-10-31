package com.safetrip.backend.infrastructure.integration.zurich.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ZurichAuthResponse {
    private String token;
    private User user;
    private String message;

    @Data
    public static class User {
        private String username;
        private String email;
        @JsonProperty("first_name")
        private String firstName;
        @JsonProperty("last_name")
        private String lastName;
    }
}