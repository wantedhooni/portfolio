package com.revy.example.saas.api.account.usecase.impl;

import com.revy.example.account.command.AccountCommand;
import com.revy.example.account.command.dto.DepositCommand;
import com.revy.example.account.command.dto.OpenAccountCommand;
import com.revy.example.account.command.dto.WithdrawCommand;
import com.revy.example.account.reader.AccountReader;
import com.revy.example.account.reader.dto.AccountResult;
import com.revy.example.account.reader.dto.AccountSearchCondition;
import com.revy.example.account.reader.dto.AccountTxResult;
import com.revy.example.account.reader.dto.AccountTxSearchCondition;
import com.revy.example.account.reader.dto.PositionLotResult;
import com.revy.example.account.reader.dto.StockPositionResult;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.saas.api.account.payload.AccountPayload;
import com.revy.example.saas.api.account.payload.AccountTxPayload;
import com.revy.example.saas.api.account.payload.PositionPayload;
import com.revy.example.saas.api.account.usecase.AccountUseCase;
import com.revy.example.saas.api.common.AccountOwnershipValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AccountUseCaseImpl implements AccountUseCase {

    private final AccountReader accountReader;
    private final AccountCommand accountCommand;
    private final AccountOwnershipValidator ownershipValidator;

    @Override
    public AccountPayload.ModelResponse openAccount(Long userId, AccountPayload.CreateRequest request) {
        Long id = accountCommand.openAccount(new OpenAccountCommand(
            userId,
            request.accountName(),
            request.accountType(),
            request.currency()
        ));
        return get(userId, id);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountPayload.ModelResponse get(Long userId, Long accountId) {
        return toModelResponse(ownershipValidator.requireOwner(userId, accountId));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiPageResponse<AccountPayload.ModelResponse> search(Long userId, Pageable pageable,
                                                                AccountPayload.SearchRequest request) {
        AccountSearchCondition condition = AccountSearchCondition.builder()
            .userId(userId)
            .accountNumber(request.accountNumber())
            .accountType(request.accountType())
            .status(request.status())
            .currency(request.currency())
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
    public AccountPayload.ModelResponse updateName(Long userId, Long accountId, AccountPayload.UpdateRequest request) {
        ownershipValidator.requireOwner(userId, accountId);
        accountCommand.updateAccountName(accountId, request.accountName());
        return get(userId, accountId);
    }

    @Override
    public void close(Long userId, Long accountId) {
        ownershipValidator.requireOwner(userId, accountId);
        accountCommand.closeAccount(accountId);
    }

    @Override
    public void deposit(Long userId, Long accountId, AccountPayload.DepositRequest request) {
        ownershipValidator.requireOwner(userId, accountId);
        accountCommand.deposit(new DepositCommand(accountId, request.amount(), request.referenceId()));
    }

    @Override
    public void withdraw(Long userId, Long accountId, AccountPayload.WithdrawRequest request) {
        ownershipValidator.requireOwner(userId, accountId);
        accountCommand.withdraw(new WithdrawCommand(accountId, request.amount(), request.referenceId()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiPageResponse<AccountTxPayload.ModelResponse> searchTransactions(Long userId, Long accountId,
                                                                              Pageable pageable,
                                                                              AccountTxPayload.SearchRequest request) {
        ownershipValidator.requireOwner(userId, accountId);
        AccountTxSearchCondition condition = AccountTxSearchCondition.builder()
            .accountId(accountId)
            .stockId(request.stockId())
            .txType(request.txType())
            .status(request.status())
            .referenceId(request.referenceId())
            .tradedAtFrom(request.tradedAtFrom())
            .tradedAtTo(request.tradedAtTo())
            .build();

        Page<AccountTxResult> result = accountReader.searchAccountTx(pageable, condition);
        return ApiPageResponse.of(
            result.getContent().stream().map(this::toModelResponse).toList(),
            result.getTotalElements(),
            result.getNumber(),
            result.getSize()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AccountTxPayload.ModelResponse getTransaction(Long userId, Long accountId, Long transactionId) {
        ownershipValidator.requireOwner(userId, accountId);
        AccountTxResult tx = accountReader.getAccountTxById(transactionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        if (!tx.accountId().equals(accountId)) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
        }
        return toModelResponse(tx);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionPayload.ModelResponse> getPositions(Long userId, Long accountId) {
        ownershipValidator.requireOwner(userId, accountId);
        return accountReader.findAllPositionsByAccountId(accountId).stream()
            .map(this::toModelResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PositionPayload.ModelResponse getPosition(Long userId, Long accountId, Long stockId) {
        ownershipValidator.requireOwner(userId, accountId);
        return accountReader.findPositionByAccountAndStock(accountId, stockId)
            .map(this::toModelResponse)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
    }

    private AccountPayload.ModelResponse toModelResponse(AccountResult account) {
        return new AccountPayload.ModelResponse(
            account.id(),
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

    private AccountTxPayload.ModelResponse toModelResponse(AccountTxResult tx) {
        return new AccountTxPayload.ModelResponse(
            tx.id(),
            tx.accountId(),
            tx.stockId(),
            tx.txType(),
            tx.amount(),
            tx.quantity(),
            tx.price(),
            tx.fee(),
            tx.tax(),
            tx.status(),
            tx.referenceId(),
            tx.tradedAt()
        );
    }

    private PositionPayload.ModelResponse toModelResponse(StockPositionResult position) {
        return new PositionPayload.ModelResponse(
            position.id(),
            position.accountId(),
            position.stockId(),
            position.totalQuantity(),
            position.realizedPnl(),
            position.lots().stream().map(this::toLotResponse).toList()
        );
    }

    private PositionPayload.LotResponse toLotResponse(PositionLotResult lot) {
        return new PositionPayload.LotResponse(
            lot.id(),
            lot.buyTxId(),
            lot.originalQuantity(),
            lot.remainingQuantity(),
            lot.buyPrice(),
            lot.boughtAt(),
            lot.lotStatus()
        );
    }
}
