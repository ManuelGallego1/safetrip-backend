package com.safetrip.backend.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvisorCodeResponse {
    private Long discountId;
    private String codigo;
    private String tipo;
    private Boolean activo;
}