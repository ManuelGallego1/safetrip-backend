package com.safetrip.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationDTO {
    private String recipient;
    private String message;
    private String subject;
}