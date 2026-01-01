package com.revy.authapp.web.service.dto;

import lombok.Getter;

public interface LoginResult {
    String getAccessToken();

    String getRefreshToken();
}
