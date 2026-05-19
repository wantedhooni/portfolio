package com.revy.example.saas.auth.mapper;

import com.revy.example.jwt.enums.PrincipalType;
import com.revy.example.jwt.payload.DefaultJwtPrincipal;
import com.revy.example.jwt.payload.JwtPrincipal;
import com.revy.example.user.reader.dto.UserCredentialResult;
import com.revy.example.user.reader.dto.UserResult;

public final class JwtPrincipalMapper {

    private JwtPrincipalMapper() {
    }

    public static JwtPrincipal toJwtPrincipal(UserResult user) {
        return new DefaultJwtPrincipal(user.id(), user.email(), user.role(), PrincipalType.USER);
    }

    public static JwtPrincipal toJwtPrincipal(UserCredentialResult user) {
        return new DefaultJwtPrincipal(user.id(), user.email(), user.role(), PrincipalType.USER);
    }
}
