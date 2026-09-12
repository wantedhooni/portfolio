package com.revy.api_server.application.web.api.auth.payload;

import com.revy.api_server.application.web.api.auth.usecase.dto.LoginCommand;
import com.revy.api_server.application.web.api.auth.usecase.dto.LoginResult;
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
    ) implements LoginCommand {
        @Override
        public String getEmail() {
            return email;
        }

        @Override
        public String getPassword() {
            return password();
        }
    }

    @Schema(name = "LoginPayload.Res")
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
