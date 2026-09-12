package com.revy.api_server.web.api.auth;

import com.revy.api_server.application.web.api.auth.AuthApi;
import com.revy.api_server.application.web.api.auth.payload.LoginPayload;
import com.revy.api_server.application.web.api.auth.payload.SignupPayload;
import com.revy.api_server.application.web.api.auth.payload.TokenReissuePayload;
import com.revy.api_server.application.web.api.auth.usecase.AuthUsecase;
import com.revy.api_server.application.web.api.auth.usecase.dto.LoginResult;
import com.revy.api_server.application.web.api.auth.usecase.dto.impl.LoginResultImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthApi 컨트롤러 단위 테스트")
class AuthApiTest {

    @Mock
    private AuthUsecase authUsecase;

    @InjectMocks
    private AuthApi authApi;

    @Test
    @DisplayName("회원가입 응답에 사용자 ID가 포함된다")
    void signup_returnsResponse() throws Exception {
        when(authUsecase.signup(any())).thenReturn(1L);

        SignupPayload.Req req = new SignupPayload.Req(
                "a@b.com",
                "pw",
                "name",
                "010",
                "addr"
        );

        ResponseEntity<SignupPayload.Res> response = authApi.signup(req);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().userId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("로그인 응답에 토큰이 포함된다")
    void login_returnsTokens() throws Exception {
        LoginResult result = LoginResultImpl.builder()
                .tokenType("Bearer")
                .accessToken("access")
                .refreshToken("refresh")
                .build();
        when(authUsecase.login(any())).thenReturn(result);

        LoginPayload.Req req = new LoginPayload.Req("a@b.com", "pw");

        ResponseEntity<LoginPayload.Res> response = authApi.login(req);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isEqualTo("access");
    }

    @Test
    @DisplayName("토큰 재발급 응답에 신규 토큰이 포함된다")
    void reissue_returnsTokens() throws Exception {
        LoginResult result = LoginResultImpl.builder()
                .tokenType("Bearer")
                .accessToken("newAccess")
                .refreshToken("newRefresh")
                .build();
        when(authUsecase.reissue("refresh")).thenReturn(result);

        TokenReissuePayload.Req req = new TokenReissuePayload.Req("refresh");

        ResponseEntity<TokenReissuePayload.Res> response = authApi.reissue(req);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isEqualTo("newAccess");
    }
}
