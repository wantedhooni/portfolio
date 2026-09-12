package com.revy.api.server.api.auth.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SignupPayload {
    @Schema(name = "SignupPayload.Req")
    public record Req(
            @Email
            @NotBlank
            String email,
            @NotBlank
            @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
            String password,
            @NotBlank
            @Size(max = 20, message = "닉네임은 20자 이하여야 합니다.")
            String nickName
    ) {
    }

    @Schema(name = "SignupPayload.Res")
    public record Res(
            String message
    ) {
    }
}
