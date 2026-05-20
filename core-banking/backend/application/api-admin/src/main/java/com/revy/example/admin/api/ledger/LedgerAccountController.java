package com.revy.example.admin.api.ledger;

import com.revy.example.admin.api.common.AbstractCrudApi;
import com.revy.example.admin.api.common.ApiConstants;
import com.revy.example.admin.api.ledger.payload.LedgerPayload;
import com.revy.example.admin.api.ledger.usecase.LedgerUseCase;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Ledger - Account", description = "계정과목 (Chart of Accounts)")
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/ledger/account")
public class LedgerAccountController extends AbstractCrudApi<
        Long,
        LedgerPayload.CreateAccountRequest,
        LedgerPayload.RenameAccountRequest,
        LedgerPayload.AccountSearchRequest,
        LedgerPayload.AccountResponse> {

    private final LedgerUseCase useCase;

    @Override
    protected ApiPageResponse<LedgerPayload.AccountResponse> getPage(
            Pageable pageable, LedgerPayload.AccountSearchRequest request) {
        return useCase.searchAccounts(pageable, request);
    }

    @Override
    protected LedgerPayload.AccountResponse doCreate(LedgerPayload.CreateAccountRequest request) {
        return useCase.createAccount(request);
    }

    @Override
    protected LedgerPayload.AccountResponse doGet(Long id) {
        return useCase.getAccount(id);
    }

    @Override
    protected LedgerPayload.AccountResponse doUpdate(Long id, LedgerPayload.RenameAccountRequest request) {
        useCase.renameAccount(id, request);
        return useCase.getAccount(id);
    }

    @Override
    protected void doDelete(Long id) {
        useCase.discontinueAccount(id);
    }

    @Operation(summary = "계정과목 사용 중단")
    @PostMapping("/{id}/discontinue")
    public ResponseEntity<ApiResponse<Void>> discontinue(@PathVariable Long id) {
        useCase.discontinueAccount(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
