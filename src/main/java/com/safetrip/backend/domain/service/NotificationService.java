package com.safetrip.backend.domain.service;

import com.safetrip.backend.application.dto.NotificationDTO;

public interface NotificationService {
    void send(NotificationDTO notification);
}