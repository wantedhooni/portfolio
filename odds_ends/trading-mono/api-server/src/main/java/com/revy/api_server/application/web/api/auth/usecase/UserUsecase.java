package com.revy.api_server.application.web.api.auth.usecase;

import com.revy.api_server.application.web.api.auth.usecase.dto.UserInfoResult;

public interface UserUsecase {
    UserInfoResult getUserInfo(Long userId);
}
