package com.revy.example.saas.auth.mapper;

import com.revy.example.jwt.enums.PrincipalType;
import com.revy.example.jwt.payload.DefaultJwtPrincipal;
import com.revy.example.jwt.payload.JwtPrincipal;
import com.revy.example.user.reader.dto.UserCredentialResult;
import com.revy.example.user.reader.dto.UserResult;

import java.util.Set;

/**
 * api-saas 의 USER 는 RBAC 비대상.
 * {@link DefaultJwtPrincipal#permissions()} 은 항상 빈 Set 으로 채운다.
 * (권한 검사는 ROLE_USER authority 기준으로만 수행)
 */
public final class JwtPrincipalMapper {

    private JwtPrincipalMapper() {
    }

    public static JwtPrincipal toJwtPrincipal(UserResult user) {
        return new DefaultJwtPrincipal(
                user.id(), user.email(), user.role(), PrincipalType.USER, Set.of()
        );
    }

    public static JwtPrincipal toJwtPrincipal(UserCredentialResult user) {
        return new DefaultJwtPrincipal(
                user.id(), user.email(), user.role(), PrincipalType.USER, Set.of()
        );
    }
}
