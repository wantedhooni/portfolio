package com.revy.api_server.application.support.impl;

import com.revy.securities.domain.account.Account;
import com.revy.securities.domain.account.repo.AccountRepo;
import com.revy.api_server.application.exception.AccountException;
import com.revy.api_server.application.support.AccountSupport;
import io.jsonwebtoken.lang.Assert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountSupportImpl implements AccountSupport {
    private final AccountRepo accountRepo;

    @Override
    @Transactional(readOnly = true)
    public Account findById(Long id) {
        return accountRepo.findById(id)
                          .orElseThrow(() -> new AccountException(AccountException.AccountErrorCode.ACCOUNT_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public Account findOneByAccountNo(String accountNo) {
        return accountRepo.findOneByAccountNo(accountNo)
                          .orElseThrow(() -> new AccountException(AccountException.AccountErrorCode.ACCOUNT_NOT_FOUND));

    }

    @Override
    @Transactional(readOnly = true)
    public Account findOneByOwnerIdAndAccountNo(Long userId, String accountNo) {
        return accountRepo.findOneByOwnerIdAndAccountNo(userId, accountNo)
                          .orElseThrow(() -> new AccountException(AccountException.AccountErrorCode.ACCOUNT_NOT_FOUND));

    }

    @Transactional
    @Override
    public Account updateBuyOrderAccountBalance(Account account, BigDecimal amount) {
        Assert.notNull(account, "Account must not be null!");
        return this.updateBuyOrderAccountBalance(account.getOwnerId(), account.getAccountNo(), amount);
    }

    @Transactional
    @Override
    public Account updateCancelOrderAccountBalance(Account account, BigDecimal amount) {
        Assert.notNull(account, "Account must not be null!");
        return this.updateCancelOrderAccountBalance(account.getOwnerId(), account.getAccountNo(), amount);
    }

    private Account updateBuyOrderAccountBalance(Long ownerId, String accountNo, BigDecimal amount) {
        Assert.notNull(ownerId, "ownerId is null");
        Assert.hasText(accountNo, "accountNo is empty");
        Assert.notNull(amount, "amount is null");
        accountRepo.subtractAvailableCashBalance(ownerId, accountNo, amount);
        return this.findOneByAccountNo(accountNo);
    }

    private Account updateCancelOrderAccountBalance(Long ownerId, String accountNo, BigDecimal amount) {
        Assert.notNull(ownerId, "ownerId is null");
        Assert.hasText(accountNo, "accountNo is empty");
        Assert.notNull(amount, "amount is null");
        accountRepo.addAvailableCashBalance(ownerId, accountNo, amount);
        return this.findOneByAccountNo(accountNo);
    }

}
