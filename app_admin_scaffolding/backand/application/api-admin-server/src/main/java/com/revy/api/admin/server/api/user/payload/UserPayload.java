package com.revy.api.admin.server.api.user.payload;


import com.revy.common.domain.enums.user.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public class UserPayload {
    @Schema(name = "UserPayload.CreateReq")
    public record CreateReq(
            @Email
            @NotBlank
            String email,
            @NotBlank
            String password,
            String firstName,
            String lastName,
            String nickName
    ) {
    }

    @Schema(name = "UserPayload.UpdateReq")
    public record UpdateReq(
            @Email
            @NotBlank
            String email,
            @NotBlank
            String password,
            String firstName,
            String lastName,
            String nickName
    ) {
    }

    @Schema(name = "UserPayload.Res")
    public record Res(
            String id,
            String email,
            String firstName,
            String lastName,
            String nickName,
            UserStatus status,
            Set<String> permissions,
            int failCount,
            boolean enabled
    ) {
    }
}
