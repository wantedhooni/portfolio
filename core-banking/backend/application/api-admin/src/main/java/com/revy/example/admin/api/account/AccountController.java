package com.revy.example.admin.api.account;

import com.revy.example.admin.api.account.payload.AccountPayload;
import com.revy.example.admin.api.account.usecase.AccountUseCase;
import com.revy.example.admin.api.common.AbstractCrudApi;
import com.revy.example.admin.api.common.ApiConstants;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/account")
public class AccountController extends
    AbstractCrudApi<Long, AccountPayload.CreateRequest, AccountPayload.UpdateRequest, AccountPayload.SearchRequest, AccountPayload.ModelResponse> {

    private final AccountUseCase useCase;

    // ── CRUD (AbstractCrudApi) ────────────────────────────────

    @Override
    protected ApiPageResponse<AccountPayload.ModelResponse> getPage(Pageable pageable,
                                                                    AccountPayload.SearchRequest searchRequest) {
        return useCase.search(pageable, searchRequest);
    }

    @Override
    protected AccountPayload.ModelResponse doCreate(AccountPayload.CreateRequest request) {
        return useCase.openAccount(request);
    }

    @Override
    protected AccountPayload.ModelResponse doGet(Long id) {
        return useCase.get(id);
    }

    @Override
    protected AccountPayload.ModelResponse doUpdate(Long id, AccountPayload.UpdateRequest request) {
        return useCase.updateName(id, request);
    }

    @Override
    protected void doDelete(Long id) {
        useCase.close(id);
    }

    // ── 커스텀 액션 ───────────────────────────────────────────

    @PostMapping("/{id}/suspend")
    public ResponseEntity<ApiResponse<Void>> suspend(@PathVariable Long id) {
        useCase.suspend(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<ApiResponse<Void>> deposit(
            @PathVariable Long id,
            @Valid @RequestBody AccountPayload.DepositRequest request
    ) {
        useCase.deposit(id, request);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @PathVariable Long id,
            @Valid @RequestBody AccountPayload.WithdrawRequest request
    ) {
        useCase.withdraw(id, request);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
