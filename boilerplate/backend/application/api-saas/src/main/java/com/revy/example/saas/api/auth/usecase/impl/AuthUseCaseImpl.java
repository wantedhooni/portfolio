package com.revy.example.saas.api.auth.usecase.impl;

import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.doamin.user.User;
import com.revy.example.jwt.JwtSessionService;
import com.revy.example.jwt.payload.JwtSession;
import com.revy.example.jwt.payload.JwtTokenPair;
import com.revy.example.jwt.payload.LoginRequest;
import com.revy.example.jwt.payload.LogoutRequest;
import com.revy.example.jwt.payload.RefreshTokenRequest;
import com.revy.example.saas.api.auth.compoenet.UserJwtPrincipal;
import com.revy.example.saas.api.auth.mapper.JwtPrincipalMapper;
import com.revy.example.saas.api.auth.payload.UserAuthResponse;
import com.revy.example.saas.api.auth.usecase.AuthUseCase;
import com.revy.example.user.reader.UserReader;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthUseCaseImpl implements AuthUseCase {
    private final UserReader userReader;
    private final PasswordEncoder passwordEncoder;
    private final JwtSessionService jwtSessionService;


    @Transactional(readOnly = true)
    public UserAuthResponse login(LoginRequest request) {
        User user = userReader.findByEmail(request.email())
                              .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return issueTokens(user);
    }

    /**
     * 저장된 관리자 리프레시 토큰을 검증하고 토큰을 회전 발급합니다.
     */
    @Transactional(readOnly = true)
    public UserAuthResponse refresh(RefreshTokenRequest request) {
        JwtSession session = jwtSessionService.refresh(request, UserJwtPrincipal.TYPE);
        JwtTokenPair tokenPair = session.tokens();
        User user = userReader.findById(session.principal().id())
                              .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserAuthResponse.of(tokenPair.accessToken(), tokenPair.refreshToken());
    }

    /**
     * 로그아웃 요청의 관리자 액세스 토큰을 블랙리스트에 넣고 리프레시 토큰을 폐기합니다.
     */
    @Transactional
    public void logout(String authorization, LogoutRequest request) {
        jwtSessionService.logout(authorization, request);
    }

    private UserAuthResponse issueTokens(User user) {
        JwtTokenPair tokenPair = jwtSessionService.issue(JwtPrincipalMapper.toJwtPrincipal(user)).tokens();
        return UserAuthResponse.of(tokenPair.accessToken(), tokenPair.refreshToken());
    }
}
