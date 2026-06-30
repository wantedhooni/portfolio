package com.revy.api_server.application.infra.security.rules;

public final class SecurityRules {
    private SecurityRules() {
    }

    public static final String[] PERMIT_ALL_PATTERNS = {
            "/favicon.ico",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/**",
            "/api/auth/**"
    };

    public static final String[] AUTHENTICATED_PATTERNS = {
            "/api/account/**",
    };

}
