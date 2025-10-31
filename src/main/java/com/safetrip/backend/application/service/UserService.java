package com.safetrip.backend.application.service;

import com.safetrip.backend.domain.model.User;
import com.safetrip.backend.web.dto.request.UserRequest;

public interface UserService {
    User updateUser(UserRequest request);
}