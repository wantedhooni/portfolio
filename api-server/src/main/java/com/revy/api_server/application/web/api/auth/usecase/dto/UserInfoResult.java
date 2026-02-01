package com.revy.api_server.application.web.api.auth.usecase.dto;

import com.revy.securities.domain.user.UserStatus;

public interface UserInfoResult {
    String getEmail();

    String getName();

    String getPhone();

    String getAddress();

    UserStatus getStatus();
}
