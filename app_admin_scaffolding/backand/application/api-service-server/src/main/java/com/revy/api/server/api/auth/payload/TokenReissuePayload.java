package com.revy.api.server.api.auth.payload;

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
    }
}
