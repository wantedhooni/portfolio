package com.revy.authapp.web.service.dto.impl;

import com.revy.authapp.web.service.dto.LoginResult;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LoginResultImpl implements LoginResult {

    String accessToken;

    String refreshToken;

    @Builder
    public LoginResultImpl(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
