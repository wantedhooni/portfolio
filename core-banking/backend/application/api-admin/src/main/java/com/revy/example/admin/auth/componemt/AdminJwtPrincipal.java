package com.revy.example.admin.auth.componemt;

import com.revy.example.jwt.enums.PrincipalType;

public class AdminJwtPrincipal {
    public static final PrincipalType TYPE = PrincipalType.ADMIN;

    private AdminJwtPrincipal() {
    }
}
