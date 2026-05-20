package com.revy.example.saas.api.common;

import com.revy.example.account.reader.AccountReader;
import com.revy.example.account.reader.dto.AccountResult;
import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountOwnershipValidator {

    private final AccountReader accountReader;

    public AccountResult requireOwner(Long userId, Long accountId) {
        AccountResult account = accountReader.getAccountById(accountId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
        if (!account.userId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return account;
    }
}
