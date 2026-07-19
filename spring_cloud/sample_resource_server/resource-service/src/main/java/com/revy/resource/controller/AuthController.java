package com.revy.resource.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
class AuthController {
    @GetMapping("/public")
    public Map<String, Object> publicApi() {
        return Map.of("message", "public endpoint", "timestamp", Instant.now().toString());
    }

    @GetMapping("/user/me")
    public Map<String, Object> currentUser(Authentication authentication, @AuthenticationPrincipal Jwt jwt) {
        log.debug("authentication: {}", authentication);
        log.debug("Jwt: {}", jwt);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "authenticated user endpoint");
        response.put("principal", authentication.getName());
        response.put("subject", jwt.getSubject());
        response.put("issuer", jwt.getIssuer());
        response.put("authorities", authentication.getAuthorities());
        response.put("claims", jwt.getClaims());
        return response;
    }

    @GetMapping("/admin/report")
    public Map<String, Object> adminApi(Authentication authentication) {
        return Map.of("message", "admin endpoint", "principal", authentication.getName(), "authorities",
                      authentication.getAuthorities());
    }
}
