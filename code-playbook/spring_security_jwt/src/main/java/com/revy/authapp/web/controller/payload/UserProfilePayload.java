package com.revy.authapp.web.controller.payload;

import com.revy.authapp.web.service.dto.UserInfoResult;

public class UserProfilePayload {

    public record Res(
            String email,
            String name,
            String phone,
            String address
    ) {

        public static UserProfilePayload.Res from(UserInfoResult userInfoResult) {
            return new UserProfilePayload.Res(
                    userInfoResult.getEmail(),
                    userInfoResult.getName(),
                    userInfoResult.getPhone(),
                    userInfoResult.getAddress());
        }
    }
}
