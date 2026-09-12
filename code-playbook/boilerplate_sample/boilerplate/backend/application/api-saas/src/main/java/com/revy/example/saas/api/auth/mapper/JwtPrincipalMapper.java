package com.revy.example.saas.api.auth.mapper;

import com.revy.example.doamin.admin.Admin;
import com.revy.example.doamin.user.User;
import com.revy.example.jwt.enums.PrincipalType;
import com.revy.example.jwt.payload.DefaultJwtPrincipal;
import com.revy.example.jwt.payload.JwtPrincipal;

public class JwtPrincipalMapper {

    private JwtPrincipalMapper() {
    }

    public static JwtPrincipal toJwtPrincipal(User user) {
        return new DefaultJwtPrincipal(user.getId(), user.getEmail(), user.getRole(), PrincipalType.USER);
    }
}
