package com.revy.example.account.command.impl;

import com.revy.example.account.command.AccountCommand;
import com.revy.example.account.command.dto.DepositCommand;
import com.revy.example.account.command.dto.OpenAccountCommand;
import com.revy.example.account.command.dto.TransferCommand;
import com.revy.example.account.command.dto.WithdrawCommand;
import com.revy.example.account.reader.AccountReader;
import com.revy.example.domain.account.Account;
import com.revy.example.domain.account.AccountTx;
import com.revy.example.domain.account.exception.AccountNotFoundException;
import com.revy.example.domain.account.exception.AccountNumberDuplicatedException;
import com.revy.example.domain.account.exception.CurrencyMismatchException;
import com.revy.example.domain.account.exception.SameAccountTransferException;

import java.math.BigDecimal;
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
        // TODO:REVY - EVENT 발행(AccountOpened) - commit after
        return account.getId();
    }

    @Override
    public void updateAccountName(Long accountId, String accountName) {
        loadAccount(accountId).updateName(accountName);
        // TODO:REVY - EVENT 발행(AccountNameUpdated) - commit after
    }

    @Override
    public void suspendAccount(Long accountId) {
        loadAccount(accountId).suspend();
        // TODO:REVY - EVENT 발행(AccountSuspended) - commit after
    }

    @Override
    public void closeAccount(Long accountId) {
        loadAccount(accountId).close();
        // TODO:REVY - EVENT 발행(AccountClosed) - commit after
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
        // TODO:REVY - EVENT 발행(AccountDeposited) - commit after
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
        // TODO:REVY - EVENT 발행(AccountWithdrawn) - commit after
    }

    @Override
    public void transfer(TransferCommand command) {
        // 1) 멱등성 — 동일 referenceId의 거래가 있으면 무시
        if (accountReader.existsTxByReferenceId(command.referenceId())) {
            log.info("Duplicate transfer ignored. referenceId={}", command.referenceId());
            return;
        }

        // 2) 자기 자신 이체 금지
        if (command.fromAccountId().equals(command.toAccountId())) {
            throw new SameAccountTransferException();
        }

        // 3) 두 계좌 로딩 + 활성·통화 검증
        Account from = loadAccount(command.fromAccountId());
        Account to   = loadAccount(command.toAccountId());

        if (!from.getCurrency().equals(to.getCurrency())) {
            throw new CurrencyMismatchException(from.getCurrency(), to.getCurrency());
        }

        BigDecimal fee = command.fee() == null ? BigDecimal.ZERO : command.fee();
        BigDecimal totalDebit = command.amount().add(fee);   // 출금측 차감액 (이체액 + 수수료)

        // 4) 출금측 처리 — 잔고 검증·차감 + 거래 기록
        from.withdraw(totalDebit);
        entityManager.persist(
            AccountTx.ofTransferOut(from.getId(), command.amount(), fee, command.referenceId())
        );

        // 5) 입금측 처리 — 입금 + 거래 기록
        to.deposit(command.amount());
        entityManager.persist(
            AccountTx.ofTransferIn(to.getId(), command.amount(), command.referenceId())
        );

        // 6) 분개 — (차) 보통예금[입금측] / (대) 보통예금[출금측] + 수수료수익 (LedgerCommand 위임은 후속 작업)
        // TODO:REVY - EVENT 발행(AccountTransferred) - commit after
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
