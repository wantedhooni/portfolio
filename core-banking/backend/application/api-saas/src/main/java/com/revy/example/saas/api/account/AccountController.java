package com.revy.example.saas.api.account;

import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.common.ApiResponse;
import com.revy.example.jwt.payload.JwtPrincipal;
import com.revy.example.saas.api.account.payload.AccountPayload;
import com.revy.example.saas.api.account.payload.AccountTxPayload;
import com.revy.example.saas.api.account.payload.PositionPayload;
import com.revy.example.saas.api.account.usecase.AccountUseCase;
import com.revy.example.saas.api.common.ApiConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/accounts")
public class AccountController {

    private final AccountUseCase useCase;

    @PostMapping
    public ResponseEntity<ApiResponse<AccountPayload.ModelResponse>> create(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody AccountPayload.CreateRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.ok(useCase.openAccount(principal.id(), request)));
    }

    @GetMapping
    public ApiResponse<ApiPageResponse<AccountPayload.ModelResponse>> search(
            @AuthenticationPrincipal JwtPrincipal principal,
            Pageable pageable,
            @Valid @ModelAttribute AccountPayload.SearchRequest request
    ) {
        return ApiResponse.ok(useCase.search(principal.id(), pageable, request));
    }

    @GetMapping("/{accountId}")
    public ApiResponse<AccountPayload.ModelResponse> get(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long accountId
    ) {
        return ApiResponse.ok(useCase.get(principal.id(), accountId));
    }

    @PatchMapping("/{accountId}")
    public ApiResponse<AccountPayload.ModelResponse> update(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long accountId,
            @Valid @RequestBody AccountPayload.UpdateRequest request
    ) {
        return ApiResponse.ok(useCase.updateName(principal.id(), accountId, request));
    }

    @DeleteMapping("/{accountId}")
    public ApiResponse<Void> close(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long accountId
    ) {
        useCase.close(principal.id(), accountId);
        return ApiResponse.ok();
    }

    @PostMapping("/{accountId}/deposit")
    public ApiResponse<Void> deposit(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long accountId,
            @Valid @RequestBody AccountPayload.DepositRequest request
    ) {
        useCase.deposit(principal.id(), accountId, request);
        return ApiResponse.ok();
    }

    @PostMapping("/{accountId}/withdraw")
    public ApiResponse<Void> withdraw(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long accountId,
            @Valid @RequestBody AccountPayload.WithdrawRequest request
    ) {
        useCase.withdraw(principal.id(), accountId, request);
        return ApiResponse.ok();
    }

    @GetMapping("/{accountId}/transactions")
    public ApiResponse<ApiPageResponse<AccountTxPayload.ModelResponse>> searchTransactions(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long accountId,
            Pageable pageable,
            @Valid @ModelAttribute AccountTxPayload.SearchRequest request
    ) {
        return ApiResponse.ok(useCase.searchTransactions(principal.id(), accountId, pageable, request));
    }

    @GetMapping("/{accountId}/transactions/{transactionId}")
    public ApiResponse<AccountTxPayload.ModelResponse> getTransaction(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long accountId,
            @PathVariable Long transactionId
    ) {
        return ApiResponse.ok(useCase.getTransaction(principal.id(), accountId, transactionId));
    }

    @GetMapping("/{accountId}/positions")
    public ApiResponse<List<PositionPayload.ModelResponse>> getPositions(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long accountId
    ) {
        return ApiResponse.ok(useCase.getPositions(principal.id(), accountId));
    }

    @GetMapping("/{accountId}/positions/{stockId}")
    public ApiResponse<PositionPayload.ModelResponse> getPosition(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long accountId,
            @PathVariable Long stockId
    ) {
        return ApiResponse.ok(useCase.getPosition(principal.id(), accountId, stockId));
    }
}
