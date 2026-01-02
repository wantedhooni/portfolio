package com.revy.authapp.web.service.dto;

public interface LoginResult {
    String getTokenType();

    String getAccessToken();

    String getRefreshToken();
}
