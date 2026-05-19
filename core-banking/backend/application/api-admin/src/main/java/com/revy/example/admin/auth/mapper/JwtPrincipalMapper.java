package com.revy.example.admin.auth.mapper;

import com.revy.example.admin.dto.AdminCredentialResult;
import com.revy.example.admin.dto.AdminResult;
import com.revy.example.jwt.enums.PrincipalType;
import com.revy.example.jwt.payload.DefaultJwtPrincipal;
import com.revy.example.jwt.payload.JwtPrincipal;

public final class JwtPrincipalMapper {

    private JwtPrincipalMapper() {
    }

    public static JwtPrincipal toJwtPrincipal(AdminResult admin) {
        return new DefaultJwtPrincipal(admin.id(), admin.email(), admin.role(), PrincipalType.ADMIN);
    }

    public static JwtPrincipal toJwtPrincipal(AdminCredentialResult admin) {
        return new DefaultJwtPrincipal(admin.id(), admin.email(), admin.role(), PrincipalType.ADMIN);
    }
}
