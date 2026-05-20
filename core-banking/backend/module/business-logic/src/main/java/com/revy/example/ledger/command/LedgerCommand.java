package com.revy.example.ledger.command;

import com.revy.example.ledger.command.dto.CreateLedgerAccountCommand;
import com.revy.example.ledger.command.dto.OpenPeriodCommand;
import com.revy.example.ledger.command.dto.PostJournalEntryCommand;

public interface LedgerCommand {

    // ── Chart of Accounts ────────────────────────────────────────
    Long createAccount(CreateLedgerAccountCommand command);
    void discontinueAccount(Long accountId);
    void renameAccount(Long accountId, String name, String description);

    // ── Period ───────────────────────────────────────────────────
    Long openPeriod(OpenPeriodCommand command);
    void closePeriod(Long periodId, Long closedByAdminId);

    // ── Journal Entry ────────────────────────────────────────────
    /**
     * 분개 생성 + 즉시 전기 (POST).
     * 차변/대변 합 일치 검증, 회계기간 OPEN 검증.
     * 일자 기준 활성 기간 자동 선택.
     * @return 생성된 JournalEntry ID
     */
    Long createAndPostJournal(PostJournalEntryCommand command);

    /**
     * 역분개 — 기존 분개의 차변/대변을 뒤집은 새 분개 생성·POST.
     * 원본 분개는 REVERSED 상태로 변경.
     */
    Long reverseJournal(Long originalJournalId, String reversalJournalNumber, String reason);
}
