package com.revy.api_server.application.web.api.auth.usecase.dto.impl;

import com.revy.securities.domain.user.UserStatus;
import com.revy.api_server.application.web.api.auth.usecase.dto.UserInfoResult;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserInfoResultImpl implements UserInfoResult {
    private String email;
    private String name;
    private String phone;
    private String address;
    private UserStatus status;

    @Builder
    public UserInfoResultImpl(String email, String name, String phone, String address, UserStatus status) {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.status = status;
    }
}
