package com.revy.api.server.api.auth.usecase.impl;

import com.revy.api.server.api.auth.payload.LoginPayload;
import com.revy.api.server.api.auth.payload.SignupPayload;
import com.revy.application.facade.user.UserProcessor;
import com.revy.application.facade.user.UserReader;
import com.revy.application.facade.user.dto.UserReaderDto;
import com.revy.common.domain.enums.user.UserStatus;
import com.revy.jwt.provider.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthUseCaseImplTest {

    @Mock
    private UserReader userReader;

    @Mock
    private UserProcessor processor;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthUseCaseImpl authUseCase;

    @BeforeEach
    void setUp() {
        authUseCase = new AuthUseCaseImpl(userReader, processor, passwordEncoder, jwtTokenProvider);
    }

    @Test
    void signupCreatesUserWithNormalizedEmail() {
        SignupPayload.Req req = new SignupPayload.Req("  USER@Example.com ", "password123", "  Revy User  ");

        when(userReader.getAuthUserByEmail("user@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");

        SignupPayload.Res response = authUseCase.signup(req);

        assertEquals("회원가입이 완료되었습니다.", response.message());
        verify(processor).signup("user@example.com", "encoded-password", null, null, "Revy User");
    }

    @Test
    void signupRejectsDuplicateEmailBeforeEncoding() {
        SignupPayload.Req req = new SignupPayload.Req("USER@example.com", "password123", "revy");
        UserReaderDto.AuthUser existingUser = new UserReaderDto.AuthUser(
                UUID.randomUUID(),
                "user@example.com",
                "encoded-password",
                UserStatus.ACTIVE,
                true,
                Set.of("SERVICE_USE")
        );

        when(userReader.getAuthUserByEmail("user@example.com")).thenReturn(Optional.of(existingUser));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> authUseCase.signup(req));

        assertEquals("존재하는 계정입니다.", error.getMessage());
        verify(passwordEncoder, never()).encode(anyString());
        verify(processor, never()).signup(eq("user@example.com"), anyString(), eq(null), eq(null), eq("revy"));
    }

    @Test
    void loginFindsUserWithNormalizedEmail() {
        LoginPayload.Req req = new LoginPayload.Req(" USER@example.com ", "password123");
        UUID userId = UUID.randomUUID();
        UserReaderDto.AuthUser user = new UserReaderDto.AuthUser(
                userId,
                "user@example.com",
                "encoded-password",
                UserStatus.ACTIVE,
                true,
                Set.of("SERVICE_USE")
        );

        when(userReader.getAuthUserByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);
        when(jwtTokenProvider.createAccessToken(userId.toString())).thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken(userId.toString())).thenReturn("refresh-token");

        LoginPayload.Res response = authUseCase.login(req);

        assertEquals("Bearer", response.tokenType());
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
    }
}
