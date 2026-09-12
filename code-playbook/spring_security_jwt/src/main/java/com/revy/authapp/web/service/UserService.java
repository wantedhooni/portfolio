package com.revy.authapp.web.service;

import com.revy.authapp.web.service.dto.UserInfoResult;

public interface UserService {
    UserInfoResult getUserInfo(Long userId);
}
