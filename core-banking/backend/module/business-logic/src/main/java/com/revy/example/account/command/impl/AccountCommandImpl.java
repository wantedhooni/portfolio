package com.revy.example.account.command.impl;

import com.revy.example.account.command.AccountCommand;
import com.revy.example.account.command.dto.DepositCommand;
import com.revy.example.account.command.dto.OpenAccountCommand;
import com.revy.example.account.command.dto.WithdrawCommand;
import com.revy.example.account.reader.AccountReader;
import com.revy.example.domain.account.Account;
import com.revy.example.domain.account.AccountTx;
import com.revy.example.domain.account.exception.AccountNotFoundException;
import com.revy.example.domain.account.exception.AccountNumberDuplicatedException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class AccountCommandImpl implements AccountCommand {

    private final EntityManager entityManager;
    private final AccountReader accountReader;

    @Override
    public Long openAccount(OpenAccountCommand command) {
        String accountNumber = generateAccountNumber();
        if (accountReader.existsByAccountNumber(accountNumber)) {
            throw new AccountNumberDuplicatedException(accountNumber);
        }

        Account account = Account.open(
            command.userId(),
            accountNumber,
            command.accountName(),
            command.accountType(),
            command.currency()
        );
        entityManager.persist(account);
        return account.getId();
    }

    @Override
    public void updateAccountName(Long accountId, String accountName) {
        loadAccount(accountId).updateName(accountName);
    }

    @Override
    public void suspendAccount(Long accountId) {
        loadAccount(accountId).suspend();
    }

    @Override
    public void closeAccount(Long accountId) {
        loadAccount(accountId).close();
    }

    @Override
    public void deposit(DepositCommand command) {
        if (accountReader.existsTxByReferenceId(command.referenceId())) {
            log.info("Duplicate deposit ignored. referenceId={}", command.referenceId());
            return;
        }

        Account account = loadAccount(command.accountId());
        account.deposit(command.amount());

        entityManager.persist(
            AccountTx.ofDeposit(command.accountId(), command.amount(), command.referenceId())
        );
    }

    @Override
    public void withdraw(WithdrawCommand command) {
        if (accountReader.existsTxByReferenceId(command.referenceId())) {
            log.info("Duplicate withdrawal ignored. referenceId={}", command.referenceId());
            return;
        }

        Account account = loadAccount(command.accountId());
        account.withdraw(command.amount());

        entityManager.persist(
            AccountTx.ofWithdrawal(command.accountId(), command.amount(), command.referenceId())
        );
    }

    // ── 내부 — 엔티티 로딩 (mutation용) ───────────────────────────
    private Account loadAccount(Long accountId) {
        Account account = entityManager.find(Account.class, accountId);
        if (account == null) {
            throw new AccountNotFoundException();
        }
        return account;
    }

    private String generateAccountNumber() {
        return "ACC-%s".formatted(UUID.randomUUID().toString().substring(0, 12).toUpperCase());
    }
}
