package com.revy.securities.domain.account.repo.query;

import com.revy.securities.domain.account.Account;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface AccountQueryRepo {

    Optional<Account> findOneByAccountNo(String accountNo);

    Optional<Account> findOneByOwnerIdAndAccountNo(Long ownerId, String accountNo);

    @Transactional
    long nextAccountNoSeq();
}
