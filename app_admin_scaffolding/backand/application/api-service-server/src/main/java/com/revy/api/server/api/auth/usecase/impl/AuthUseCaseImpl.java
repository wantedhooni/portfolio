package com.revy.api.server.api.auth.usecase.impl;


import com.revy.api.server.api.auth.payload.LoginPayload;
import com.revy.api.server.api.auth.payload.SignupPayload;
import com.revy.api.server.api.auth.payload.TokenReissuePayload;
import com.revy.api.server.api.auth.usecase.AuthUseCase;
import com.revy.application.facade.user.UserProcessor;
import com.revy.application.facade.user.UserReader;
import com.revy.application.facade.user.dto.UserReaderDto;
import com.revy.common.domain.enums.user.UserStatus;
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

    private final UserReader userReader;
    private final UserProcessor processor;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public SignupPayload.Res signup(SignupPayload.Req req) {
        String normalizedEmail = normalizeEmail(req.email());
        userReader.getAuthUserByEmail(normalizedEmail).ifPresent(user -> {
            throw new IllegalArgumentException("존재하는 계정입니다.");
        });

        String encodedPassword = passwordEncoder.encode(req.password());
        String normalizedNickName = normalizeNickname(req.nickName());

        processor.signup(normalizedEmail, encodedPassword, null, null, normalizedNickName);
        return new SignupPayload.Res("회원가입이 완료되었습니다.");
    }

    @Override
    public LoginPayload.Res login(LoginPayload.Req req) {
        String normalizedEmail = normalizeEmail(req.email());

        UserReaderDto.AuthUser user = userReader.getAuthUserByEmail(normalizedEmail)
                                                .orElseThrow(
                                                        () -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!user.enabled() || user.status() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("비활성화된 계정입니다.");
        }

        if (!passwordEncoder.matches(req.password(), user.encodedPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.id().toString());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.id().toString());
        return new LoginPayload.Res("Bearer", accessToken, refreshToken);
    }

    @Override
    public TokenReissuePayload.Res reissue(TokenReissuePayload.Req req) {

        if (!jwtTokenProvider.validateToken(req.refreshToken())) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }
        String id = jwtTokenProvider.getUserId(req.refreshToken());
        UUID userId = UUID.fromString(id);

        UserReaderDto.AuthUser user = userReader.getByAuthUserId(userId)
                                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!user.enabled() || user.status() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("비활성화된 계정입니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.id().toString());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.id().toString());
        return new TokenReissuePayload.Res("Bearer", accessToken, refreshToken);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String normalizeNickname(String nickName) {
        return nickName.trim();
    }

}
