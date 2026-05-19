package com.revy.example.admin.api.account.usecase.impl;

import com.revy.example.account.command.AccountCommand;
import com.revy.example.account.command.dto.DepositCommand;
import com.revy.example.account.command.dto.OpenAccountCommand;
import com.revy.example.account.command.dto.WithdrawCommand;
import com.revy.example.account.reader.AccountReader;
import com.revy.example.account.reader.dto.AccountResult;
import com.revy.example.account.reader.dto.AccountSearchCondition;
import com.revy.example.admin.api.account.payload.AccountPayload;
import com.revy.example.admin.api.account.usecase.AccountUseCase;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountUseCaseImpl implements AccountUseCase {

    private final AccountReader  accountReader;
    private final AccountCommand accountCommand;

    @Override
    public AccountPayload.ModelResponse openAccount(AccountPayload.CreateRequest request) {
        Long id = accountCommand.openAccount(new OpenAccountCommand(
            request.userId(),
            request.accountName(),
            request.accountType(),
            request.currency()
        ));
        return get(id);
    }

    @Override
    public AccountPayload.ModelResponse get(Long id) {
        return accountReader.getAccountById(id)
            .map(this::toModelResponse)
            .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    @Override
    public ApiPageResponse<AccountPayload.ModelResponse> search(Pageable pageable,
                                                                AccountPayload.SearchRequest searchRequest) {
        AccountSearchCondition condition = AccountSearchCondition.builder()
            .userId(searchRequest.userId())
            .accountNumber(searchRequest.accountNumber())
            .accountType(searchRequest.accountType())
            .status(searchRequest.status())
            .currency(searchRequest.currency())
            .build();
        Page<AccountResult> result = accountReader.searchAccounts(pageable, condition);
        return ApiPageResponse.of(
            result.getContent().stream().map(this::toModelResponse).toList(),
            result.getTotalElements(),
            result.getNumber(),
            result.getSize()
        );
    }

    @Override
    public AccountPayload.ModelResponse updateName(Long id, AccountPayload.UpdateRequest request) {
        accountCommand.updateAccountName(id, request.accountName());
        return get(id);
    }

    @Override
    public void suspend(Long id) {
        accountCommand.suspendAccount(id);
    }

    @Override
    public void close(Long id) {
        accountCommand.closeAccount(id);
    }

    @Override
    public void deposit(Long id, AccountPayload.DepositRequest request) {
        accountCommand.deposit(new DepositCommand(id, request.amount(), request.referenceId()));
    }

    @Override
    public void withdraw(Long id, AccountPayload.WithdrawRequest request) {
        accountCommand.withdraw(new WithdrawCommand(id, request.amount(), request.referenceId()));
    }

    private AccountPayload.ModelResponse toModelResponse(AccountResult account) {
        return new AccountPayload.ModelResponse(
            account.id(),
            account.userId(),
            account.accountNumber(),
            account.accountName(),
            account.accountType(),
            account.currency(),
            account.balance(),
            account.availableBalance(),
            account.status(),
            account.createdAt(),
            account.updatedAt()
        );
    }
}
