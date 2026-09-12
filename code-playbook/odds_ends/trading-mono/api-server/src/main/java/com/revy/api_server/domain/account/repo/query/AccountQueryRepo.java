package com.revy.api_server.domain.account.repo.query;

import com.revy.api_server.domain.account.Account;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface AccountQueryRepo {

    Optional<Account> findOneByAccountNo(String accountNo);

    Optional<Account> findOneByOwnerIdAndAccountNo(Long ownerId, String accountNo);

    @Transactional
    long nextAccountNoSeq();
}
