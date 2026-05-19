package com.revy.example.admin.api.account.usecase;

import com.revy.example.admin.api.account.payload.AccountPayload;
import com.revy.example.core.common.ApiPageResponse;
import org.springframework.data.domain.Pageable;

public interface AccountUseCase {

    AccountPayload.ModelResponse create(AccountPayload.CreateRequest request);

    AccountPayload.ModelResponse get(Long id);

    AccountPayload.ModelResponse update(Long id, AccountPayload.UpdateRequest request);

    void delete(Long id);

    ApiPageResponse<AccountPayload.ModelResponse> search(Pageable pageable, AccountPayload.SearchRequest searchRequest);
}
