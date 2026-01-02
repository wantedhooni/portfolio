package com.revy.authapp.web.controller.payload;

import com.revy.authapp.web.service.dto.LoginCommand;
import com.revy.authapp.web.service.dto.LoginResult;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginPayload {
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
