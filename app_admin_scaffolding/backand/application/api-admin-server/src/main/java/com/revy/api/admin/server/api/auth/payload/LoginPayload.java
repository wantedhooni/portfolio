package com.revy.api.admin.server.api.auth.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginPayload {

    @Schema(name = "LoginPayload.Req")
    public record Req(
            @Email
            @NotBlank
            String email,
            @NotBlank
            String password
    ) {
    }

    @Schema(name = "LoginPayload.Res")
    public record Res(
            String tokenType,
            String accessToken,
            String refreshToken

    ) {
    }
}
