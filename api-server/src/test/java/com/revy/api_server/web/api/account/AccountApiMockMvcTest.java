package com.revy.api_server.web.api.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revy.api_server.application.web.api.account.AccountApi;
import com.revy.securities.domain.account.enums.AccountStatus;
import com.revy.securities.domain.account.enums.AccountType;
import com.revy.securities.domain.user.Role;
import com.revy.securities.domain.user.User;
import com.revy.api_server.application.web.api.account.payload.CreateAccountPayload;
import com.revy.api_server.application.web.api.account.payload.DepositAccountPayload;
import com.revy.api_server.application.web.api.account.payload.MyAccountsPayload;
import com.revy.api_server.application.web.api.account.usecase.AccountUseCase;
import com.revy.api_server.application.infra.security.UserPrincipal;
import com.revy.common.enums.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountApi MockMvc 슬라이스 테스트")
class AccountApiMockMvcTest {

    @Mock
    private AccountUseCase accountUseCase;

    @InjectMocks
    private AccountApi accountApi;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        authentication = auth();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        mockMvc = MockMvcBuilders.standaloneSetup(accountApi)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("계좌 생성 요청 시 계좌번호를 반환한다")
    void createAccount_returnsAccountNo() throws Exception {
        CreateAccountPayload.Res res = new CreateAccountPayload.Res(
                AccountType.CASH,
                "acc",
                Currency.USD,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
        when(accountUseCase.create(any(), any(), any())).thenReturn(res);

        CreateAccountPayload.Req req = new CreateAccountPayload.Req(AccountType.CASH, Currency.USD);

        mockMvc.perform(post("/api/account/create")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNo").value("acc"));
    }

    @Test
    @DisplayName("내 계좌 목록 요청 시 리스트를 반환한다")
    void getMyAccounts_returnsList() throws Exception {
        List<MyAccountsPayload.Res> res = List.of(
                new MyAccountsPayload.Res(
                        AccountType.CASH,
                        "acc",
                        Currency.USD,
                        AccountStatus.ACTIVE,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                )
        );
        when(accountUseCase.getMyAccounts(any(), any())).thenReturn(res);

        mockMvc.perform(get("/api/account/myAccounts").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountNo").value("acc"));
    }

    @Test
    @DisplayName("입금 요청 시 성공 응답을 반환한다")
    void deposit_returnsSuccess() throws Exception {
        doNothing().when(accountUseCase).deposit(any(), any(), any());
        DepositAccountPayload.Req req = new DepositAccountPayload.Req("acc", BigDecimal.TEN);

        mockMvc.perform(post("/api/account/deposit")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private Authentication auth() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "publicId", UUID.randomUUID());
        user.setEmail("a@b.com");
        Role role = new Role("USER");
        user.addRole(role);
        UserPrincipal principal = UserPrincipal.from(user);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }
}
