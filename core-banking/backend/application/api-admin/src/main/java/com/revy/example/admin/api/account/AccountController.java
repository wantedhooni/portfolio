package com.revy.example.admin.api.account;

import com.revy.example.admin.api.account.payload.AccountPayload;
import com.revy.example.admin.api.account.usecase.AccountUseCase;
import com.revy.example.admin.api.common.AbstractCrudApi;
import com.revy.example.admin.api.common.ApiConstants;
import com.revy.example.core.common.ApiPageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/account")
public class AccountController extends
    AbstractCrudApi<Long, AccountPayload.CreateRequest, AccountPayload.UpdateRequest, AccountPayload.SearchRequest, AccountPayload.ModelResponse> {

    private final AccountUseCase useCase;

    @Override
    protected ApiPageResponse<AccountPayload.ModelResponse> getPage(Pageable pageable,
                                                                    AccountPayload.SearchRequest searchRequest) {
        return useCase.search(pageable, searchRequest);
    }

    @Override
    protected AccountPayload.ModelResponse doCreate(AccountPayload.CreateRequest request) {
        return useCase.create(request);
    }

    @Override
    protected AccountPayload.ModelResponse doGet(Long id) {
        return useCase.get(id);
    }

    @Override
    protected AccountPayload.ModelResponse doUpdate(Long id, AccountPayload.UpdateRequest request) {
        return useCase.update(id, request);
    }

    @Override
    protected void doDelete(Long id) {
        useCase.delete(id);
    }
}

