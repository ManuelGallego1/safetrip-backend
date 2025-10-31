package com.safetrip.backend.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProcessRequest {
    String description;
    String value;
    Long parameterId;
}
