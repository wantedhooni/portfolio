package com.revy.example.admin.api.auth.mapper;

import com.revy.example.doamin.admin.Admin;
import com.revy.example.jwt.enums.PrincipalType;
import com.revy.example.jwt.payload.DefaultJwtPrincipal;
import com.revy.example.jwt.payload.JwtPrincipal;

public class JwtPrincipalMapper {

    public static JwtPrincipal toJwtPrincipal(Admin admin) {
        return new DefaultJwtPrincipal(admin.getId(), admin.getEmail(), admin.getRole(), PrincipalType.ADMIN);
    }
}
