package com.revy.api_server.application.support;

import com.revy.securities.domain.account.Account;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

public interface AccountSupport {
    Account findById(Long id);

    Account findOneByAccountNo(String accountNo);

    Account findOneByOwnerIdAndAccountNo(Long userId, String accountNo);

    @Transactional
    Account updateBuyOrderAccountBalance(Account account, BigDecimal amount);

    @Transactional
    Account updateCancelOrderAccountBalance(Account account, BigDecimal amount);
}
