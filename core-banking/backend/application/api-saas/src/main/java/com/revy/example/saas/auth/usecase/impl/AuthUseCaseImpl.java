package com.revy.example.saas.auth.usecase.impl;

import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.jwt.JwtSessionService;
import com.revy.example.jwt.payload.JwtSession;
import com.revy.example.jwt.payload.JwtTokenPair;
import com.revy.example.jwt.payload.LoginRequest;
import com.revy.example.jwt.payload.LogoutRequest;
import com.revy.example.jwt.payload.RefreshTokenRequest;
import com.revy.example.saas.auth.compoenet.UserJwtPrincipal;
import com.revy.example.saas.auth.mapper.JwtPrincipalMapper;
import com.revy.example.saas.auth.payload.UserAuthResponse;
import com.revy.example.saas.auth.usecase.AuthUseCase;
import com.revy.example.user.reader.UserReader;
import com.revy.example.user.reader.dto.UserCredentialResult;
import com.revy.example.user.reader.dto.UserResult;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthUseCaseImpl implements AuthUseCase {

    private final UserReader        userReader;
    private final PasswordEncoder   passwordEncoder;
    private final JwtSessionService jwtSessionService;

    @Transactional(readOnly = true)
    public UserAuthResponse login(LoginRequest request) {
        UserCredentialResult user = userReader.findCredentialByEmail(request.email())
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.encodedPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return issueTokens(user);
    }

    @Transactional(readOnly = true)
    public UserAuthResponse refresh(RefreshTokenRequest request) {
        JwtSession session = jwtSessionService.refresh(request, UserJwtPrincipal.TYPE);
        JwtTokenPair tokenPair = session.tokens();
        UserResult user = userReader.findById(session.principal().id())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserAuthResponse.of(tokenPair.accessToken(), tokenPair.refreshToken());
    }

    @Transactional
    public void logout(String authorization, LogoutRequest request) {
        jwtSessionService.logout(authorization, request);
    }

    private UserAuthResponse issueTokens(UserCredentialResult user) {
        JwtTokenPair tokenPair = jwtSessionService.issue(JwtPrincipalMapper.toJwtPrincipal(user)).tokens();
        return UserAuthResponse.of(tokenPair.accessToken(), tokenPair.refreshToken());
    }
}
