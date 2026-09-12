package com.revy.api_server.application.web.api.auth.payload;

import com.revy.api_server.application.web.api.auth.usecase.dto.LoginResult;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class TokenReissuePayload {
    @Schema(name = "TokenReissuePayload.Req")
    public record Req(
            @NotBlank
            String refreshToken
    ) {

    }

    @Schema(name = "TokenReissuePayload.Res")
    public record Res(
            String tokenType,
            String accessToken,
            String refreshToken

    ) {
        public static Res from(LoginResult result) {
            return new Res(
                    result.getTokenType(),
                    result.getAccessToken(),
                    result.getRefreshToken()
            );
        }
    }
}
