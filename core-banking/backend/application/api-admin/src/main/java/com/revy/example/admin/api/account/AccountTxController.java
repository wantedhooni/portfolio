package com.revy.example.admin.api.account;

import com.revy.example.admin.api.account.payload.AccountTxPayload;
import com.revy.example.admin.api.account.usecase.AccountTxUseCase;
import com.revy.example.admin.api.common.AbstractCrudApi;
import com.revy.example.admin.api.common.ApiConstants;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.common.ApiResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/account_tx")
public class AccountTxController extends
    AbstractCrudApi<Long, AccountTxPayload.CreateRequest, AccountTxPayload.UpdateRequest, AccountTxPayload.SearchRequest, AccountTxPayload.ModelResponse> {

    private final AccountTxUseCase useCase;

    @Override
    protected ApiPageResponse<AccountTxPayload.ModelResponse> getPage(Pageable pageable,
                                                                      AccountTxPayload.SearchRequest searchRequest) {
        PageImpl<AccountTxPayload.ModelResponse> result = useCase.search(pageable, searchRequest);
        return ApiPageResponse.of(result.getContent(), result.getTotalElements(), result.getNumber(), result.getSize());
    }

    @Override
    protected AccountTxPayload.ModelResponse doGet(Long id) {
        return useCase.get(id);
    }

    @Hidden
    @Override
    public ResponseEntity<ApiResponse<AccountTxPayload.ModelResponse>> create(AccountTxPayload.CreateRequest request) {
        throw new UnsupportedOperationException("Not supported operation");
    }

    @Hidden
    @Override
    public ResponseEntity<ApiResponse<AccountTxPayload.ModelResponse>> update(Long aLong,
                                                                              AccountTxPayload.UpdateRequest request) {
        throw new UnsupportedOperationException("Not supported operation");
    }

    @Hidden
    @Override
    public ResponseEntity<ApiResponse<Void>> delete(Long aLong) {
        throw new UnsupportedOperationException("Not supported operation");
    }


    @Override
    protected AccountTxPayload.ModelResponse doCreate(AccountTxPayload.CreateRequest request) {
        throw new UnsupportedOperationException("Not supported operation: create");
    }

    @Override
    protected AccountTxPayload.ModelResponse doUpdate(Long id, AccountTxPayload.UpdateRequest request) {
        throw new UnsupportedOperationException("Not supported operation: update");
    }

    @Override
    protected void doDelete(Long id) {
        throw new UnsupportedOperationException("Not supported operation: delete");
    }
}

