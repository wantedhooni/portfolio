package com.revy.example.admin.api.ledger.usecase;

import com.revy.example.admin.api.ledger.payload.LedgerPayload;
import com.revy.example.core.common.ApiPageResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface LedgerUseCase {

    // ── LedgerAccount ────────────────────────────────────────────
    LedgerPayload.AccountResponse createAccount(LedgerPayload.CreateAccountRequest request);
    LedgerPayload.AccountResponse getAccount(Long id);
    ApiPageResponse<LedgerPayload.AccountResponse> searchAccounts(Pageable pageable,
                                                                   LedgerPayload.AccountSearchRequest request);
    void renameAccount(Long id, LedgerPayload.RenameAccountRequest request);
    void discontinueAccount(Long id);

    // ── AccountingPeriod ─────────────────────────────────────────
    LedgerPayload.PeriodResponse openPeriod(LedgerPayload.OpenPeriodRequest request);
    LedgerPayload.PeriodResponse getPeriod(Long id);
    void closePeriod(Long id, LedgerPayload.ClosePeriodRequest request);

    // ── JournalEntry ─────────────────────────────────────────────
    LedgerPayload.JournalResponse postJournal(LedgerPayload.PostJournalRequest request);
    LedgerPayload.JournalResponse getJournal(Long id);
    ApiPageResponse<LedgerPayload.JournalResponse> searchJournals(Pageable pageable,
                                                                   LedgerPayload.JournalSearchRequest request);
    LedgerPayload.JournalResponse reverseJournal(Long id, LedgerPayload.ReverseJournalRequest request);

    // ── Reports ──────────────────────────────────────────────────
    List<LedgerPayload.TrialBalanceLine> trialBalance(LocalDate from, LocalDate to);
}
