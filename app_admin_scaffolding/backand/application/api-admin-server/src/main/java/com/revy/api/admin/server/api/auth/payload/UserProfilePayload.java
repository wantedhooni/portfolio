package com.revy.api.admin.server.api.auth.payload;

import io.swagger.v3.oas.annotations.media.Schema;

public class UserProfilePayload {

    @Schema(name = "UserProfilePayload.Res")
    public record Res(
            String email,
            String name,
            String phone,
            String address
    ) {
    }
}
