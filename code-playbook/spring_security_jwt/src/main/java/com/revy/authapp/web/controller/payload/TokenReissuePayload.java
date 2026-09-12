package com.revy.authapp.web.controller.payload;

import com.revy.authapp.web.service.dto.LoginResult;
import jakarta.validation.constraints.NotBlank;

public class TokenReissuePayload {
    public record Req(
            @NotBlank
            String refreshToken
    ) {

    }

    public record Res(
            String tokenType,
            String accessToken,
            String refreshToken

    ) {
        public static TokenReissuePayload.Res from(LoginResult result) {
            return new TokenReissuePayload.Res(
                    result.getTokenType(),
                    result.getAccessToken(),
                    result.getRefreshToken()
            );
        }
    }
}
