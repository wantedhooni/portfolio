package com.revy.authapp.web.service.dto;

import com.revy.authapp.domain.user.UserStatus;

public interface UserInfoResult {
    String getEmail();

    String getName();

    String getPhone();

    String getAddress();

    UserStatus getStatus();
}
