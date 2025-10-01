package com.safetrip.backend.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private String message;
    private String status;
    private T data;
    private Pagination pagination;

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(message, "success", data, null);
    }

    public static <T> ApiResponse<T> success(String message, T data, Pagination pagination) {
        return new ApiResponse<>(message, "success", data, pagination);
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(message, "error", data, null);
    }

    public static <T> ApiResponse<T> error(String message, T data, Pagination pagination) {
        return new ApiResponse<>(message, "error", data, pagination);
    }
}