package com.revy.example.saas.api.account.usecase;

import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.saas.api.account.payload.AccountPayload;
import com.revy.example.saas.api.account.payload.AccountTxPayload;
import com.revy.example.saas.api.account.payload.PositionPayload;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AccountUseCase {

    AccountPayload.ModelResponse openAccount(Long userId, AccountPayload.CreateRequest request);

    AccountPayload.ModelResponse get(Long userId, Long accountId);

    ApiPageResponse<AccountPayload.ModelResponse> search(Long userId, Pageable pageable,
                                                         AccountPayload.SearchRequest request);

    AccountPayload.ModelResponse updateName(Long userId, Long accountId, AccountPayload.UpdateRequest request);

    void close(Long userId, Long accountId);

    void deposit(Long userId, Long accountId, AccountPayload.DepositRequest request);

    void withdraw(Long userId, Long accountId, AccountPayload.WithdrawRequest request);

    /** 계좌이체 — 출금 계좌(accountId)는 본인 소유여야 함, 입금 계좌는 누구든 가능 */
    void transfer(Long userId, Long accountId, AccountPayload.TransferRequest request);

    ApiPageResponse<AccountTxPayload.ModelResponse> searchTransactions(Long userId, Long accountId,
                                                                       Pageable pageable,
                                                                       AccountTxPayload.SearchRequest request);

    AccountTxPayload.ModelResponse getTransaction(Long userId, Long accountId, Long transactionId);

    List<PositionPayload.ModelResponse> getPositions(Long userId, Long accountId);

    PositionPayload.ModelResponse getPosition(Long userId, Long accountId, Long stockId);
}
