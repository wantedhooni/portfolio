package com.revy.api_server.web.api.account.usecase.impl;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.api_server.application.web.api.account.usecase.impl.AccountUseCaseImpl;
import com.revy.securities.domain.account.Account;
import com.revy.securities.domain.account.enums.AccountType;
import com.revy.securities.domain.account.repo.AccountRepo;
import com.revy.api_server.application.web.api.account.payload.CreateAccountPayload;
import com.revy.api_server.application.web.api.account.payload.MyAccountsPayload;
import com.revy.common.enums.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountUseCaseImpl 서비스 테스트")
class AccountUseCaseImplTest {

    @Mock
    private AccountRepo accountRepo;
    @Mock
    private JPAQueryFactory jpaQueryFactory;
    @Mock
    private JPAQuery<MyAccountsPayload.Res> jpaQuery;

    @InjectMocks
    private AccountUseCaseImpl accountUseCase;

    @Test
    @DisplayName("계좌 생성 시 저장되고 응답이 반환된다")
    void create_savesAccountAndReturnsResponse() {
        Account saved = Account.createNewAccount(1L, "09900010", AccountType.CASH, Currency.USD);
        when(accountRepo.save(any(Account.class))).thenReturn(saved);

        CreateAccountPayload.Res res = accountUseCase.create(1L, AccountType.CASH, Currency.USD);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepo).save(captor.capture());
        assertThat(captor.getValue().getOwnerId()).isEqualTo(1L);
        assertThat(res.currency()).isEqualTo("USD");
        assertThat(res.accountNo()).isNotBlank();
    }

    @Test
    @DisplayName("내 계좌 조회 시 결과를 반환한다")
    void getMyAccounts_nullReq_returnsFetchedResults() {
        doReturn(jpaQuery).when(jpaQueryFactory).select(org.mockito.Mockito.<Expression<MyAccountsPayload.Res>>any());
        doReturn(jpaQuery).when(jpaQuery).from(org.mockito.Mockito.<EntityPath<?>>any());
        doReturn(jpaQuery).when(jpaQuery).where(org.mockito.Mockito.<Predicate>any());
        doReturn(jpaQuery).when(jpaQuery).orderBy(org.mockito.Mockito.<OrderSpecifier<?>>any());

        List<MyAccountsPayload.Res> expected = List.of(
                new MyAccountsPayload.Res(AccountType.CASH, "123", Currency.USD, null, BigDecimal.ZERO, BigDecimal.ZERO)
        );
        when(jpaQuery.fetch()).thenReturn(expected);

        List<MyAccountsPayload.Res> result = accountUseCase.getMyAccounts(1L, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).accountNo()).isEqualTo("123");
    }

    @Test
    @DisplayName("입금 시 잔고가 증가된다")
    void deposit_positiveAmount_updatesBalance() {
        Account account = Account.createNewAccount(1L, "09900010", AccountType.CASH, Currency.USD);
        when(accountRepo.findOneByOwnerIdAndAccountNo(1L, "123")).thenReturn(Optional.of(account));

        accountUseCase.deposit(1L, "123", BigDecimal.TEN);

        verify(accountRepo).addAllBalance("123", BigDecimal.TEN);
    }

    @Test
    @DisplayName("입금 금액이 0 이하이면 예외가 발생한다")
    void deposit_nonPositiveAmount_throwsException() {
        assertThatThrownBy(() -> accountUseCase.deposit(1L, "123", BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
