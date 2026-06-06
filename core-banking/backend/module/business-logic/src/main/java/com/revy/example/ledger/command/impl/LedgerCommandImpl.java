package com.revy.example.ledger.command.impl;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.domain.ledger.AccountingPeriod;
import com.revy.example.domain.ledger.JournalEntry;
import com.revy.example.domain.ledger.LedgerAccount;
import com.revy.example.domain.ledger.exception.AccountingPeriodNotFoundException;
import com.revy.example.domain.ledger.exception.JournalEntryNotFoundException;
import com.revy.example.domain.ledger.exception.LedgerAccountNotFoundException;
import com.revy.example.ledger.command.LedgerCommand;
import com.revy.example.ledger.command.dto.CreateLedgerAccountCommand;
import com.revy.example.ledger.command.dto.JournalLineInput;
import com.revy.example.ledger.command.dto.OpenPeriodCommand;
import com.revy.example.ledger.command.dto.PostJournalEntryCommand;
import com.revy.example.ledger.reader.LedgerReader;
import com.revy.example.ledger.reader.dto.AccountingPeriodResult;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class LedgerCommandImpl implements LedgerCommand {

    private final EntityManager entityManager;
    private final LedgerReader  ledgerReader;

    // ── Chart of Accounts ────────────────────────────────────────

    @Override
    public Long createAccount(CreateLedgerAccountCommand command) {
        if (ledgerReader.existsAccountByCode(command.accountCode())) {
            throw new BusinessException(ErrorCode.LEDGER_ACCOUNT_DUPLICATED, "code=" + command.accountCode());
        }
        LedgerAccount a = LedgerAccount.create(
            command.accountCode(), command.name(), command.category(),
            command.currency(), command.parentId(), command.description()
        );
        entityManager.persist(a);
        // TODO:REVY - EVENT 발행(LedgerAccountCreated) - commit after
        return a.getId();
    }

    @Override
    public void discontinueAccount(Long accountId) {
        loadAccount(accountId).discontinue();
        // TODO:REVY - EVENT 발행(LedgerAccountDiscontinued) - commit after
    }

    @Override
    public void renameAccount(Long accountId, String name, String description) {
        loadAccount(accountId).rename(name, description);
        // TODO:REVY - EVENT 발행(LedgerAccountRenamed) - commit after
    }

    // ── Period ───────────────────────────────────────────────────

    @Override
    public Long openPeriod(OpenPeriodCommand command) {
        AccountingPeriod p = AccountingPeriod.open(
            command.fiscalYear(), command.fiscalPeriod(),
            command.startDate(), command.endDate()
        );
        entityManager.persist(p);
        // TODO:REVY - EVENT 발행(AccountingPeriodOpened) - commit after
        return p.getId();
    }

    @Override
    public void closePeriod(Long periodId, Long closedByAdminId) {
        loadPeriod(periodId).close(closedByAdminId, Instant.now());
        // TODO:REVY - EVENT 발행(AccountingPeriodClosed) - commit after
    }

    // ── Journal Entry ────────────────────────────────────────────

    @Override
    public Long createAndPostJournal(PostJournalEntryCommand command) {
        // 1) 기간 자동 선택 (entry_date가 속한 OPEN 기간)
        AccountingPeriodResult periodResult = ledgerReader.findPeriodContaining(command.entryDate())
            .orElseThrow(AccountingPeriodNotFoundException::new);
        AccountingPeriod period = entityManager.find(AccountingPeriod.class, periodResult.id());
        period.validateOpen();

        // 2) DRAFT 생성
        JournalEntry entry = JournalEntry.draft(
            command.journalNumber(), command.entryDate(), period.getId(),
            command.description(), command.referenceType(), command.referenceId()
        );

        // 3) 라인 추가
        for (JournalLineInput line : command.lines()) {
            // 라인의 ledger account 존재/활성 검증
            LedgerAccount acc = entityManager.find(LedgerAccount.class, line.ledgerAccountId());
            if (acc == null) throw new LedgerAccountNotFoundException();
            if (line.debit() != null && line.debit().signum() > 0) {
                entry.addDebit(line.ledgerAccountId(), line.debit(), line.currency(), line.description());
            } else {
                entry.addCredit(line.ledgerAccountId(), line.credit(), line.currency(), line.description());
            }
        }

        // 4) persist 먼저 (cascade ALL로 라인도 함께)
        entityManager.persist(entry);

        // 5) post — 합계 검증 포함
        entry.post(Instant.now());

        // TODO:REVY - EVENT 발행(JournalEntryPosted) - commit after
        return entry.getId();
    }

    @Override
    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3,
               backoff = @Backoff(delay = 50, multiplier = 2.0, maxDelay = 300, random = true))
    public Long reverseJournal(Long originalJournalId, String reversalJournalNumber, String reason) {
        JournalEntry original = entityManager.find(JournalEntry.class, originalJournalId);
        if (original == null) throw new JournalEntryNotFoundException();

        // 기간 OPEN 검증 (원본 기간 사용)
        AccountingPeriod period = entityManager.find(AccountingPeriod.class, original.getPeriodId());
        if (period == null) throw new AccountingPeriodNotFoundException();
        period.validateOpen();

        JournalEntry reversal = original.createReversal(
            reversalJournalNumber, original.getEntryDate(), original.getPeriodId(), reason);
        entityManager.persist(reversal);
        reversal.post(Instant.now());

        original.markReversedBy(reversal.getId());
        // TODO:REVY - EVENT 발행(JournalEntryReversed) - commit after
        return reversal.getId();
    }

    // ── 내부 ─────────────────────────────────────────────────────

    private LedgerAccount loadAccount(Long id) {
        LedgerAccount a = entityManager.find(LedgerAccount.class, id);
        if (a == null) throw new LedgerAccountNotFoundException();
        return a;
    }

    private AccountingPeriod loadPeriod(Long id) {
        AccountingPeriod p = entityManager.find(AccountingPeriod.class, id);
        if (p == null) throw new AccountingPeriodNotFoundException();
        return p;
    }
}
