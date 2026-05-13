package com.revy.example.saas.auth.compoenet;

import com.revy.example.jwt.enums.PrincipalType;

public class UserJwtPrincipal {
    public static final PrincipalType TYPE = PrincipalType.USER;

    private UserJwtPrincipal() {
    }
}
