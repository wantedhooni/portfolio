package com.revy.security.filter;

import com.revy.jwt.provider.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Reusable JWT authentication filter.
 * Concrete projects only need to provide principal lookup and authentication conversion logic.
 */
public abstract class AbstractJwtAuthenticationFilter<T> extends OncePerRequestFilter {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String MDC_USER_ID = "USER_ID";

    private final JwtTokenProvider jwtTokenProvider;

    protected AbstractJwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken == null || bearerToken.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!bearerToken.startsWith(BEARER_PREFIX)) {
            throw new BadCredentialsException("Invalid authorization header");
        }

        String token = bearerToken.substring(BEARER_PREFIX.length());
        if (!jwtTokenProvider.validateToken(token)) {
            throw new BadCredentialsException("Invalid access token");
        }

        String userId = jwtTokenProvider.getUserId(token);
        T principal = resolvePrincipal(userId).orElseThrow(() -> new BadCredentialsException("Invalid access token"));
        if (!isActivePrincipal(principal)) {
            throw new BadCredentialsException("Inactive account");
        }

        String mdcUserId = extractMdcUserId(principal);
        if (mdcUserId != null && !mdcUserId.isBlank()) {
            MDC.put(MDC_USER_ID, mdcUserId);
        }

        Authentication authentication = toAuthentication(principal);
        if (authentication == null) {
            throw new AuthenticationServiceException("Authentication must not be null");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (mdcUserId != null && !mdcUserId.isBlank()) {
                MDC.remove(MDC_USER_ID);
            }
        }
    }

    protected abstract Optional<T> resolvePrincipal(String userId);

    protected abstract boolean isActivePrincipal(T principal);

    protected abstract Authentication toAuthentication(T principal);

    protected String extractMdcUserId(T principal) {
        return null;
    }
}

