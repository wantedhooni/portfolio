package com.revy.example.admin.api.account.usecase.impl;

import com.revy.example.account.reader.AccountReader;
import com.revy.example.account.reader.dto.AccountTxSearchCondition;
import com.revy.example.admin.api.account.payload.AccountTxPayload;
import com.revy.example.admin.api.account.usecase.AccountTxUseCase;
import com.revy.example.admin.api.admin.payload.AdminPayload;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.domain.account.AccountTx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountTxUseCaseImpl implements AccountTxUseCase {

    private final AccountReader accountReader;

    @Override
    public AccountTxPayload.ModelResponse get(Long id) {
        // TODO: REVY - NotFound Exception 예외 처리 개선 필요
        return toModelResponse(accountReader.getAccountTxById(id).orElseThrow(
                () -> new RuntimeException("AccountTx not found with id: " + id)));
    }


    @Override
    public PageImpl<AccountTxPayload.ModelResponse> search(Pageable pageable,
                                                                  AccountTxPayload.SearchRequest searchRequest) {

        AccountTxSearchCondition searchCondition = AccountTxSearchCondition.builder().build();
        Page<AccountTx> result = accountReader.searchAccountTx(pageable, searchCondition);
        return new PageImpl<AccountTxPayload.ModelResponse>(toModelResponse(result.getContent()), pageable, result.getTotalElements());
    }

    private List<AccountTxPayload.ModelResponse> toModelResponse(List<AccountTx> content) {
        return content.stream().map(this::toModelResponse).toList();
    }

    private AccountTxPayload.ModelResponse toModelResponse(AccountTx accountTx) {
        return new AccountTxPayload.ModelResponse(accountTx.getId(), accountTx.getAccountId(), accountTx.getStockId(),
                                                  accountTx.getTxType(), accountTx.getAmount(), accountTx.getQuantity(),
                                                  accountTx.getPrice(), accountTx.getFee(), accountTx.getTax(),
                                                  accountTx.getStatus(), accountTx.getReferenceId(),
                                                  accountTx.getTradedAt());
    }

}
