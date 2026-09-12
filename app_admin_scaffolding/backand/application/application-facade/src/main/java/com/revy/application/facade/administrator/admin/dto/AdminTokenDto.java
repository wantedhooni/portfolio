package com.revy.application.facade.administrator.admin.dto;

public class AdminTokenDto {
    public record Result(
            String tokenType,
            String accessToken,
            String refreshToken
    ) {
    }
}
