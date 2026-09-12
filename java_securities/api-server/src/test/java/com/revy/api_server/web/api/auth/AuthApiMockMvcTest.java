package com.revy.api_server.web.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revy.api_server.application.web.api.auth.AuthApi;
import com.revy.api_server.application.web.api.auth.payload.LoginPayload;
import com.revy.api_server.application.web.api.auth.payload.SignupPayload;
import com.revy.api_server.application.web.api.auth.payload.TokenReissuePayload;
import com.revy.api_server.application.web.api.auth.usecase.AuthUsecase;
import com.revy.api_server.application.web.api.auth.usecase.dto.LoginResult;
import com.revy.api_server.application.web.api.auth.usecase.dto.impl.LoginResultImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthApi MockMvc 슬라이스 테스트")
class AuthApiMockMvcTest {

    @Mock
    private AuthUsecase authUsecase;

    @InjectMocks
    private AuthApi authApi;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authApi).build();
    }

    @Test
    @DisplayName("회원가입 요청 시 200과 메시지를 반환한다")
    void signup_returnsOk() throws Exception {
        when(authUsecase.signup(any())).thenReturn(1L);
        SignupPayload.Req req = new SignupPayload.Req("a@b.com", "pw", "name", "010", "addr");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."));
    }

    @Test
    @DisplayName("로그인 요청 시 토큰을 반환한다")
    void login_returnsTokens() throws Exception {
        LoginResult result = LoginResultImpl.builder()
                .tokenType("Bearer")
                .accessToken("access")
                .refreshToken("refresh")
                .build();
        when(authUsecase.login(any())).thenReturn(result);
        LoginPayload.Req req = new LoginPayload.Req("a@b.com", "pw");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.refreshToken").value("refresh"));
    }

    @Test
    @DisplayName("토큰 재발급 요청 시 신규 토큰을 반환한다")
    void reissue_returnsTokens() throws Exception {
        LoginResult result = LoginResultImpl.builder()
                .tokenType("Bearer")
                .accessToken("newAccess")
                .refreshToken("newRefresh")
                .build();
        when(authUsecase.reissue("refresh")).thenReturn(result);
        TokenReissuePayload.Req req = new TokenReissuePayload.Req("refresh");

        mockMvc.perform(post("/api/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newAccess"))
                .andExpect(jsonPath("$.refreshToken").value("newRefresh"));
    }
}
