package com.revy.example.admin.api.account.usecase;

import com.revy.example.admin.api.account.payload.AccountPayload;
import com.revy.example.core.common.ApiPageResponse;
import org.springframework.data.domain.Pageable;

public interface AccountUseCase {

    AccountPayload.ModelResponse openAccount(AccountPayload.CreateRequest request);

    AccountPayload.ModelResponse get(Long id);

    ApiPageResponse<AccountPayload.ModelResponse> search(Pageable pageable, AccountPayload.SearchRequest searchRequest);

    AccountPayload.ModelResponse updateName(Long id, AccountPayload.UpdateRequest request);

    void suspend(Long id);

    void close(Long id);

    void deposit(Long id, AccountPayload.DepositRequest request);

    void withdraw(Long id, AccountPayload.WithdrawRequest request);
}
