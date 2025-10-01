package com.safetrip.backend.application.service;

import com.safetrip.backend.application.dto.NotificationDTO;

public interface NotificationService {
    void send(NotificationDTO notification);
}