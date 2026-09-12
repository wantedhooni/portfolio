package com.revy.api_server.web.api.auth.usecase.impl;

import com.revy.api_server.application.web.api.auth.usecase.impl.AuthUsecaseImpl;
import com.revy.securities.domain.user.Role;
import com.revy.securities.domain.user.User;
import com.revy.securities.domain.user.UserDetail;
import com.revy.securities.domain.user.UserStatus;
import com.revy.securities.domain.user.repo.RoleRepository;
import com.revy.securities.domain.user.repo.UserDetailRepository;
import com.revy.securities.domain.user.repo.UserQueryRepository;
import com.revy.securities.domain.user.repo.UserRepository;
import com.revy.api_server.application.web.api.auth.usecase.dto.LoginCommand;
import com.revy.api_server.application.web.api.auth.usecase.dto.LoginResult;
import com.revy.api_server.application.web.api.auth.usecase.dto.SignupCommand;
import com.revy.api_server.application.infra.security.provider.JwtTokenProvider;
import com.revy.api_server.application.infra.security.token.TokenStore;
import com.revy.common.error.ApiException;
import com.revy.common.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl 서비스 테스트")
class AuthUsecaseImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserQueryRepository userQueryRepository;
    @Mock
    private UserDetailRepository userDetailRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private TokenStore tokenStore;

    @InjectMocks
    private AuthUsecaseImpl authService;

    @Test
    @DisplayName("회원가입 시 중복 이메일이면 예외가 발생한다")
    void signup_duplicateEmail_throwsApiException() {
        when(userQueryRepository.findByEmail("a@b.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.signup(stubSignup("a@b.com")))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL.getCode());
    }

    @Test
    @DisplayName("회원가입 성공 시 사용자와 상세정보가 저장된다")
    void signup_success_savesUserAndDetail() {
        Role role = new Role("USER");
        when(userQueryRepository.findByEmail("a@b.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));

        User savedUser = new User();
        savedUser.setEmail("a@b.com");
        savedUser.setStatus(UserStatus.ACTIVE);
        ReflectionTestUtils.setField(savedUser, "id", 10L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        Long userId = authService.signup(stubSignup("a@b.com"));

        assertThat(userId).isEqualTo(10L);
        verify(userDetailRepository).save(any(UserDetail.class));
    }

    @Test
    @DisplayName("로그인 시 비밀번호가 다르면 예외가 발생한다")
    void login_invalidPassword_throwsApiException() {
        User user = new User();
        user.setEmail("a@b.com");
        user.setPassword("encoded");
        when(userQueryRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(stubLogin("a@b.com", "wrong")))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.INVALID_PASSWORD.getCode());
    }

    @Test
    @DisplayName("로그인 성공 시 토큰을 반환하고 리프레시 토큰을 저장한다")
    void login_success_returnsTokensAndStoresRefresh() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setPassword("encoded");

        when(userQueryRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pw", "encoded")).thenReturn(true);
        when(jwtTokenProvider.createAccessToken(user)).thenReturn("access");
        when(jwtTokenProvider.createRefreshToken(user)).thenReturn("refresh");
        when(jwtTokenProvider.getExpiration("refresh")).thenReturn(Instant.now().plusSeconds(3600));

        LoginResult result = authService.login(stubLogin("a@b.com", "pw"));

        assertThat(result.getAccessToken()).isEqualTo("access");
        assertThat(result.getRefreshToken()).isEqualTo("refresh");
        verify(tokenStore).saveRefreshToken(anyLong(), any(), any(Duration.class));
    }

    @Test
    @DisplayName("리프레시 토큰이 유효하지 않으면 재발급이 실패한다")
    void reissue_invalidToken_throwsApiException() {
        when(jwtTokenProvider.validateToken("bad")).thenReturn(false);

        assertThatThrownBy(() -> authService.reissue("bad"))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN.getCode());
    }

    @Test
    @DisplayName("저장된 리프레시 토큰과 다르면 재발급이 실패한다")
    void reissue_tokenMismatch_throwsApiException() {
        when(jwtTokenProvider.validateToken("refresh")).thenReturn(true);
        when(jwtTokenProvider.getUserId("refresh")).thenReturn(1L);
        when(tokenStore.findRefreshToken(1L)).thenReturn(Optional.of("other"));

        assertThatThrownBy(() -> authService.reissue("refresh"))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.REFRESH_TOKEN_MISMATCH.getCode());
    }

    @Test
    @DisplayName("재발급 성공 시 신규 토큰을 반환하고 저장한다")
    void reissue_success_returnsNewTokensAndStoresRefresh() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);

        when(jwtTokenProvider.validateToken("refresh")).thenReturn(true);
        when(jwtTokenProvider.getUserId("refresh")).thenReturn(1L);
        when(tokenStore.findRefreshToken(1L)).thenReturn(Optional.of("refresh"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.createAccessToken(user)).thenReturn("newAccess");
        when(jwtTokenProvider.createRefreshToken(user)).thenReturn("newRefresh");
        when(jwtTokenProvider.getExpiration("newRefresh")).thenReturn(Instant.now().plusSeconds(3600));

        LoginResult result = authService.reissue("refresh");

        assertThat(result.getAccessToken()).isEqualTo("newAccess");
        verify(tokenStore).saveRefreshToken(anyLong(), any(), any(Duration.class));
    }

    @Test
    @DisplayName("로그아웃 시 토큰이 유효하지 않으면 예외가 발생한다")
    void logout_invalidToken_throwsApiException() {
        when(jwtTokenProvider.validateToken("bad")).thenReturn(false);

        assertThatThrownBy(() -> authService.logout("bad"))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.INVALID_TOKEN.getCode());
    }

    @Test
    @DisplayName("로그아웃 시 액세스 토큰을 블랙리스트 처리하고 리프레시 토큰을 삭제한다")
    void logout_success_blacklistsAccessAndRemovesRefresh() {
        when(jwtTokenProvider.validateToken("access")).thenReturn(true);
        when(jwtTokenProvider.getUserId("access")).thenReturn(2L);
        when(jwtTokenProvider.getExpiration("access")).thenReturn(Instant.now().plusSeconds(600));

        authService.logout("access");

        verify(tokenStore).blacklistAccessToken(any(), any(Duration.class));
        verify(tokenStore).deleteRefreshToken(2L);
    }

    @Test
    @DisplayName("탈퇴 시 사용자가 없으면 예외가 발생한다")
    void withdraw_userNotFound_throwsApiException() {
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.withdraw(9L))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.USER_NOT_FOUND.getCode());
    }

    @Test
    @DisplayName("탈퇴 성공 시 상태가 변경되고 리프레시 토큰이 삭제된다")
    void withdraw_success_marksUserWithdrawn() {
        User user = new User();
        user.setStatus(UserStatus.ACTIVE);
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        doNothing().when(tokenStore).deleteRefreshToken(3L);

        authService.withdraw(3L);

        assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
        verify(tokenStore).deleteRefreshToken(3L);
    }

    private SignupCommand stubSignup(String email) {
        return new SignupCommand() {
            @Override
            public String getEmail() {
                return email;
            }

            @Override
            public String getPassword() {
                return "pw";
            }

            @Override
            public String getName() {
                return "name";
            }

            @Override
            public String getPhone() {
                return "010";
            }

            @Override
            public String getAddress() {
                return "addr";
            }
        };
    }

    private LoginCommand stubLogin(String email, String password) {
        return new LoginCommand() {
            @Override
            public String getEmail() {
                return email;
            }

            @Override
            public String getPassword() {
                return password;
            }
        };
    }
}
