package com.revy.api_server.application.web.api.auth.usecase.dto.impl;

import com.revy.api_server.application.web.api.auth.usecase.dto.LoginResult;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LoginResultImpl implements LoginResult {

    String tokenType;
    String accessToken;
    String refreshToken;

    @Builder

    public LoginResultImpl(String tokenType, String accessToken, String refreshToken) {
        this.tokenType = tokenType;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
