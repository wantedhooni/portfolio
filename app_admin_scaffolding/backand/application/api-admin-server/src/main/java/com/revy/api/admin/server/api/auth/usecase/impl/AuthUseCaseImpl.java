package com.revy.api.admin.server.api.auth.usecase.impl;

import com.revy.api.admin.server.api.auth.payload.LoginPayload;
import com.revy.api.admin.server.api.auth.payload.SignupPayload;
import com.revy.api.admin.server.api.auth.payload.TokenReissuePayload;
import com.revy.api.admin.server.api.auth.usecase.AuthUseCase;
import com.revy.application.facade.administrator.admin.AdminProcessor;
import com.revy.application.facade.administrator.admin.AdminReader;
import com.revy.application.facade.administrator.admin.dto.AdminReaderDto;
import com.revy.common.domain.enums.admin.AdminStatus;
import com.revy.jwt.provider.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthUseCaseImpl implements AuthUseCase {
    private final AdminReader adminReader;
    private final AdminProcessor adminProcessor;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginPayload.Res login(LoginPayload.Req req) {
        AdminReaderDto.AuthAdmin authAdmin = adminReader.getAuthAdminByEmail(req.email())
                                                        .orElseThrow(() -> new IllegalArgumentException(
                                                                "이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!authAdmin.enabled() || authAdmin.status() != AdminStatus.ACTIVE) {
            throw new IllegalArgumentException("비활성화된 계정입니다.");
        }

        if (!passwordEncoder.matches(req.password(), authAdmin.encodedPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        return issueToken(authAdmin.id());
    }

    @Override
    public TokenReissuePayload.Res reissue(TokenReissuePayload.Req req) {

        String refreshToken = req.refreshToken();
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }
        String userId = jwtTokenProvider.getUserId(refreshToken);
        UUID adminId = UUID.fromString(userId);
        AdminReaderDto.AuthAdmin admin = adminReader.getAuthAdminById(adminId)
                                                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        if (!admin.enabled() || admin.status() != AdminStatus.ACTIVE) {
            throw new IllegalArgumentException("비활성화된 계정입니다.");
        }
        return reissueToken(admin.id());
    }


    private LoginPayload.Res issueToken(UUID id) {
        String accessToken = jwtTokenProvider.createAccessToken(id.toString());
        String refreshToken = jwtTokenProvider.createRefreshToken(id.toString());
        return new LoginPayload.Res("Bearer", accessToken, refreshToken);
    }

    private TokenReissuePayload.Res reissueToken(UUID id) {
        String accessToken = jwtTokenProvider.createAccessToken(id.toString());
        String refreshToken = jwtTokenProvider.createRefreshToken(id.toString());
        return new TokenReissuePayload.Res("Bearer", accessToken, refreshToken);
    }
}
