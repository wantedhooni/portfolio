package com.revy.example.jwt;

import com.revy.example.jwt.enums.JwtTokenType;
import com.revy.example.jwt.payload.JwtPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** 요청의 Bearer 토큰을 검증하고 주입된 로더로 인증 주체를 복원하는 필터입니다. */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtPrincipalResolver jwtPrincipalResolver;
    private final JwtTokenBlacklist jwtTokenBlacklist;

    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            JwtPrincipalResolver jwtPrincipalResolver,
            JwtTokenBlacklist jwtTokenBlacklist
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtPrincipalResolver = jwtPrincipalResolver;
        this.jwtTokenBlacklist = jwtTokenBlacklist;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization != null && authorization.startsWith(JwtConstants.BEARER_PREFIX)) {
            authenticate(authorization.substring(JwtConstants.BEARER_PREFIX.length()));
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(String token) {
        try {
            Claims claims = jwtTokenProvider.parseClaims(token);
            if (jwtTokenProvider.getTokenType(claims) != JwtTokenType.ACCESS) {
                return;
            }
            String tokenId = jwtTokenProvider.getTokenId(claims);
            if (jwtTokenBlacklist.contains(tokenId)) {
                return;
            }
            Long principalId = Long.valueOf(claims.getSubject());
            String principalType = claims.get(JwtConstants.PRINCIPAL_TYPE_CLAIM, String.class);
            jwtPrincipalResolver.resolve(principalType, principalId).ifPresent(this::setAuthentication);
        } catch (JwtException | IllegalArgumentException ignored) {
            SecurityContextHolder.clearContext();
        }
    }

    private void setAuthentication(JwtPrincipal principal) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + principal.role()));
        if (principal.permissions() != null) {
            principal.permissions().forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
        }
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
