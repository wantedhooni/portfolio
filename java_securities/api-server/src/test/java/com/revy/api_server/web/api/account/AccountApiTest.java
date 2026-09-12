package com.revy.api_server.web.api.account;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountApi 컨트롤러 단위 테스트")
class AccountApiTest {

    @Mock
    private AccountUseCase accountUseCase;

    @InjectMocks
    private AccountApi accountApi;

    @Test
    @DisplayName("계좌 생성 응답이 반환된다")
    void createAccount_returnsResponse() throws Exception {
        CreateAccountPayload.Res res = new CreateAccountPayload.Res(
                AccountType.CASH,
                "acc",
                Currency.USD,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
        when(accountUseCase.create(any(), any(), any())).thenReturn(res);

        CreateAccountPayload.Req req = new CreateAccountPayload.Req(
                AccountType.CASH,
                Currency.USD
        );

        CreateAccountPayload.Res response = accountApi.createAccount(auth(), req);

        assertThat(response).isNotNull();
        assertThat(response.accountNo()).isEqualTo("acc");
    }

    @Test
    @DisplayName("내 계좌 목록 조회 결과를 반환한다")
    void getMyAccounts_returnsList() throws Exception {
        List<MyAccountsPayload.Res> res = List.of(
                new MyAccountsPayload.Res(
                        AccountType.CASH,
                        "acc",
                        Currency.USD,
                        AccountStatus.ACTIVE,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO)
        );
        when(accountUseCase.getMyAccounts(any(), any())).thenReturn(res);

        List<MyAccountsPayload.Res> result = accountApi.getMyAccounts(auth(), null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).accountNo()).isEqualTo("acc");
    }

    @Test
    @DisplayName("입금 요청 시 성공 응답을 반환한다")
    void depositAccount_returnsSuccess() throws Exception {
        doNothing().when(accountUseCase).deposit(any(), any(), any());

        DepositAccountPayload.Req req = new DepositAccountPayload.Req("acc", BigDecimal.TEN);

        DepositAccountPayload.Res result = accountApi.deposit(auth(), req);

        assertThat(result.success()).isTrue();
    }

    private UserPrincipal auth() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "publicId", UUID.randomUUID());
        user.setEmail("a@b.com");
        Role role = new Role("USER");
        user.addRole(role);
        return UserPrincipal.from(user);
    }
}
