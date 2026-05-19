package com.revy.example.account.reader;

import com.revy.example.account.reader.dto.AccountTxSearchCondition;
import com.revy.example.domain.account.Account;
import com.revy.example.domain.account.AccountTx;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface AccountReader {
    Optional<Account> getAccountById(Long id);

    Optional<AccountTx> getAccountTxById(Long id);

    Page<AccountTx> searchAccountTx(Pageable pageable, AccountTxSearchCondition condition);
}
