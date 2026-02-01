package com.revy.api_server.application.infra.filter;

import com.revy.api_server.application.infra.security.UserPrincipal;
import com.revy.securities.domain.user.User;
import com.revy.securities.domain.user.UserStatus;
import com.revy.securities.domain.user.repo.UserRepository;
import com.revy.api_server.application.exception.AuthException;
import com.revy.api_server.application.infra.security.provider.JwtTokenProvider;
import com.revy.api_server.application.infra.security.token.TokenStore;
import com.revy.common.error.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Optional;

/**
 * JWT 인증을 처리하는 필터.
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String MDC_USER_ID = "userId";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final TokenStore tokenStore;
    private final HandlerExceptionResolver resolver;


    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserRepository userRepository, TokenStore tokenStore, @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.tokenStore = tokenStore;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        log.debug("token: {}", token);
        if (StringUtils.hasText(token)) {
            if (!jwtTokenProvider.validateToken(token)) {
                resolver.resolveException(request, response, null, new AuthException(ErrorCode.INVALID_TOKEN));
                return;
            }
            if (tokenStore.isAccessTokenBlacklisted(token)) {
                resolver.resolveException(request, response, null, new AuthException(ErrorCode.EXPIRE_TOKEN));
                return;
            }

            Long userId = jwtTokenProvider.getUserId(token);
            Optional<User> user = userRepository.findById(userId);
            if (!user.isPresent()) {
                resolver.resolveException(request, response, null, new AuthException(ErrorCode.USER_NOT_FOUND));
                return;
                // 유효하지 않은 사용자인 경우 필터 체인을 계속 진행하지 않고 종료
            }
            MDC.put(MDC_USER_ID, String.valueOf(userId));
            UserPrincipal principal = UserPrincipal.from(user.get());

            if(user.get().getStatus() != UserStatus.ACTIVE) {
                resolver.resolveException(request, response, null, new AuthException(ErrorCode.INACTIVE_USER));
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

        }


        filterChain.doFilter(request, response);
    }

    /**
     * Authorization 헤더에서 Bearer 토큰을 추출한다.
     *
     * @param request HTTP 요청
     * @return 토큰 문자열
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
