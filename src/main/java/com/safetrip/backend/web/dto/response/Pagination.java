package com.safetrip.backend.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pagination {
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}