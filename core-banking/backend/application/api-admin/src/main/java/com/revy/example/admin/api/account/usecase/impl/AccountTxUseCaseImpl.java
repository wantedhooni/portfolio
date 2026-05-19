package com.revy.example.admin.api.account.usecase.impl;

import com.revy.example.account.reader.AccountReader;
import com.revy.example.account.reader.dto.AccountTxResult;
import com.revy.example.account.reader.dto.AccountTxSearchCondition;
import com.revy.example.admin.api.account.payload.AccountTxPayload;
import com.revy.example.admin.api.account.usecase.AccountTxUseCase;
import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountTxUseCaseImpl implements AccountTxUseCase {

    private final AccountReader accountReader;

    @Override
    public AccountTxPayload.ModelResponse get(Long id) {
        AccountTxResult tx = accountReader.getAccountTxById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        return toModelResponse(tx);
    }

    @Override
    public PageImpl<AccountTxPayload.ModelResponse> search(Pageable pageable,
                                                           AccountTxPayload.SearchRequest searchRequest) {
        AccountTxSearchCondition condition = AccountTxSearchCondition.builder()
            .id(searchRequest.id())
            .accountId(searchRequest.accountId())
            .stockId(searchRequest.stockId())
            .txType(searchRequest.txType())
            .status(searchRequest.status())
            .referenceId(searchRequest.referenceId())
            .build();

        Page<AccountTxResult> result = accountReader.searchAccountTx(pageable, condition);
        return new PageImpl<>(toModelResponse(result.getContent()), pageable, result.getTotalElements());
    }

    private List<AccountTxPayload.ModelResponse> toModelResponse(List<AccountTxResult> content) {
        return content.stream().map(this::toModelResponse).toList();
    }

    private AccountTxPayload.ModelResponse toModelResponse(AccountTxResult tx) {
        return new AccountTxPayload.ModelResponse(
            tx.id(), tx.accountId(), tx.stockId(), tx.txType(),
            tx.amount(), tx.quantity(), tx.price(), tx.fee(), tx.tax(),
            tx.status(), tx.referenceId(), tx.tradedAt()
        );
    }
}
