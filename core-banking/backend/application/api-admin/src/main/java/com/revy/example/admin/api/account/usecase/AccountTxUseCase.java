package com.revy.example.admin.api.account.usecase;

import com.revy.example.admin.api.account.payload.AccountTxPayload;
import com.revy.example.core.common.ApiPageResponse;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public interface AccountTxUseCase {
    AccountTxPayload.ModelResponse get(Long id);

    PageImpl<AccountTxPayload.ModelResponse> search(Pageable pageable, AccountTxPayload.SearchRequest searchRequest);
}
