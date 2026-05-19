package com.revy.example.admin.api.account.usecase.impl;

import com.revy.example.admin.api.account.payload.AccountPayload;
import com.revy.example.admin.api.account.usecase.AccountUseCase;
import com.revy.example.core.common.ApiPageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountUseCaseImpl implements AccountUseCase {


    @Override
    @Transactional
    public AccountPayload.ModelResponse create(AccountPayload.CreateRequest request) {
        return null;
    }

    @Override
    public AccountPayload.ModelResponse get(Long id) {
        return null;
    }

    @Override
    @Transactional
    public AccountPayload.ModelResponse update(Long id, AccountPayload.UpdateRequest request) {
        return null;
    }

    @Override
    @Transactional
    public void delete(Long id) {

    }

    @Override
    public ApiPageResponse<AccountPayload.ModelResponse> search(Pageable pageable,
                                                                AccountPayload.SearchRequest searchRequest) {
        return null;
    }
}
