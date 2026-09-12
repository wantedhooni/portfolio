package com.revy.api_server.application.web.api.account.usecase;

import com.revy.securities.domain.account.enums.AccountType;
import com.revy.api_server.application.web.api.account.payload.CreateAccountPayload;
import com.revy.api_server.application.web.api.account.payload.MyAccountsPayload;
import com.revy.api_server.application.web.api.account.payload.TransferPayload;
import com.revy.common.enums.Currency;

import java.math.BigDecimal;
import java.util.List;

public interface AccountUseCase {
    CreateAccountPayload.Res create(Long userId, AccountType accountType, Currency currency);

    List<MyAccountsPayload.Res> getMyAccounts(Long id, MyAccountsPayload.Req req);

    void deposit(Long userId, String accountNo, BigDecimal amount);

    void withdraw(Long userId, String accountNo, BigDecimal amount);
    TransferPayload.Res transfer(Long userId, TransferPayload.Req req);
}
