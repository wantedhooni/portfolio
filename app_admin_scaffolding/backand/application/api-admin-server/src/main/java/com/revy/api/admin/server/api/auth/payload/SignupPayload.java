package com.revy.api.admin.server.api.auth.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class SignupPayload {
    @Schema(name = "SignupPayload.Req")
    public record Req(
            @Email
            @NotBlank
            String email,
            @NotBlank
            String password
    ) {
    }

    @Schema(name = "SignupPayload.Res")
    public record Res(
            String message
    ) {
    }
}
