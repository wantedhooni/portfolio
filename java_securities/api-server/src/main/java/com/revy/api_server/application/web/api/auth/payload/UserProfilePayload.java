package com.revy.api_server.application.web.api.auth.payload;

import com.revy.api_server.application.web.api.auth.usecase.dto.UserInfoResult;
import io.swagger.v3.oas.annotations.media.Schema;

public class UserProfilePayload {

    @Schema(name = "UserProfilePayload.Res")
    public record Res(
            String email,
            String name,
            String phone,
            String address
    ) {

        public static Res from(UserInfoResult userInfoResult) {
            return new Res(
                    userInfoResult.getEmail(),
                    userInfoResult.getName(),
                    userInfoResult.getPhone(),
                    userInfoResult.getAddress());
        }
    }
}
