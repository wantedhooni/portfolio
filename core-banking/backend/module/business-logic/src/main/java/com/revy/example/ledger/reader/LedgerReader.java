package com.revy.example.ledger.reader;

import com.revy.example.ledger.reader.dto.AccountingPeriodResult;
import com.revy.example.ledger.reader.dto.JournalEntryResult;
import com.revy.example.ledger.reader.dto.JournalEntrySearchCondition;
import com.revy.example.ledger.reader.dto.LedgerAccountResult;
import com.revy.example.ledger.reader.dto.LedgerAccountSearchCondition;
import com.revy.example.ledger.reader.dto.TrialBalanceLine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LedgerReader {

    // ── LedgerAccount ────────────────────────────────────────────
    Optional<LedgerAccountResult> findAccountById(Long id);
    Optional<LedgerAccountResult> findAccountByCode(String accountCode);
    boolean existsAccountByCode(String accountCode);
    Page<LedgerAccountResult> searchAccounts(Pageable pageable, LedgerAccountSearchCondition condition);

    // ── AccountingPeriod ─────────────────────────────────────────
    Optional<AccountingPeriodResult> findPeriodById(Long id);
    Optional<AccountingPeriodResult> findPeriodByYearAndMonth(Integer fiscalYear, Integer fiscalPeriod);
    /** 특정 일자가 속한 기간 — JournalEntry 생성 시 기간 자동 선택 */
    Optional<AccountingPeriodResult> findPeriodContaining(LocalDate date);

    // ── JournalEntry ─────────────────────────────────────────────
    Optional<JournalEntryResult> findEntryById(Long id);
    Optional<JournalEntryResult> findEntryByNumber(String journalNumber);
    Page<JournalEntryResult> searchEntries(Pageable pageable, JournalEntrySearchCondition condition);

    // ── Reports ──────────────────────────────────────────────────
    /**
     * 시산표 (Trial Balance) — POSTED 상태의 분개만 집계.
     * 기간 필터링 가능 (null이면 전체).
     */
    List<TrialBalanceLine> trialBalance(LocalDate from, LocalDate to);
}
