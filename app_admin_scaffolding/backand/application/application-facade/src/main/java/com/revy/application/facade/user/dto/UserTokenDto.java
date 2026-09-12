package com.revy.application.facade.user.dto;

public class UserTokenDto {
    public record Result(
            String tokenType,
            String accessToken,
            String refreshToken
    ) {
    }
}
