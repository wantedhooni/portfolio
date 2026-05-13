package com.revy.example.admin.auth.usecase.impl;

import com.revy.example.admin.auth.componemt.AdminJwtPrincipal;
import com.revy.example.admin.auth.mapper.JwtPrincipalMapper;
import com.revy.example.admin.auth.payload.AdminAuthResponse;
import com.revy.example.admin.auth.usecase.AuthUseCase;
import com.revy.example.admin.reader.AdminReader;
import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.domain.admin.Admin;
import com.revy.example.jwt.payload.JwtSession;
import com.revy.example.jwt.JwtSessionService;
import com.revy.example.jwt.payload.JwtTokenPair;
import com.revy.example.jwt.payload.LoginRequest;
import com.revy.example.jwt.payload.LogoutRequest;
import com.revy.example.jwt.payload.RefreshTokenRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AuthUseCaseImpl implements AuthUseCase {

    private final AdminReader  adminReader;
    private final PasswordEncoder passwordEncoder;
    private final JwtSessionService jwtSessionService;


    @Transactional(readOnly = true)
    public AdminAuthResponse login(LoginRequest request) {
        Admin admin = adminReader.findByEmail(request.email())
                                     .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return issueTokens(admin);
    }

    /** 저장된 관리자 리프레시 토큰을 검증하고 토큰을 회전 발급합니다. */
    @Transactional(readOnly = true)
    public AdminAuthResponse refresh(RefreshTokenRequest request) {
        JwtSession session = jwtSessionService.refresh(request, AdminJwtPrincipal.TYPE);
        JwtTokenPair tokenPair = session.tokens();
        Admin admin = adminReader.findById(session.principal().id())
                                     .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return AdminAuthResponse.of(tokenPair.accessToken(), tokenPair.refreshToken());
    }

    /** 로그아웃 요청의 관리자 액세스 토큰을 블랙리스트에 넣고 리프레시 토큰을 폐기합니다. */
    @Transactional
    public void logout(String authorization, LogoutRequest request) {
        jwtSessionService.logout(authorization, request);
    }

    private AdminAuthResponse issueTokens(Admin admin) {
        JwtTokenPair tokenPair = jwtSessionService.issue(JwtPrincipalMapper.toJwtPrincipal(admin)).tokens();
        return AdminAuthResponse.of(tokenPair.accessToken(), tokenPair.refreshToken());
    }
}
