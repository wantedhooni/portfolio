package com.revy.example.admin.api.ledger.usecase.impl;

import com.revy.example.admin.api.ledger.payload.LedgerPayload;
import com.revy.example.admin.api.ledger.usecase.LedgerUseCase;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.ledger.command.LedgerCommand;
import com.revy.example.ledger.command.dto.CreateLedgerAccountCommand;
import com.revy.example.ledger.command.dto.JournalLineInput;
import com.revy.example.ledger.command.dto.OpenPeriodCommand;
import com.revy.example.ledger.command.dto.PostJournalEntryCommand;
import com.revy.example.ledger.reader.LedgerReader;
import com.revy.example.ledger.reader.dto.AccountingPeriodResult;
import com.revy.example.ledger.reader.dto.JournalEntryResult;
import com.revy.example.ledger.reader.dto.JournalEntrySearchCondition;
import com.revy.example.ledger.reader.dto.JournalLineResult;
import com.revy.example.ledger.reader.dto.LedgerAccountResult;
import com.revy.example.ledger.reader.dto.LedgerAccountSearchCondition;
import com.revy.example.ledger.reader.dto.TrialBalanceLine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LedgerUseCaseImpl implements LedgerUseCase {

    private final LedgerReader  ledgerReader;
    private final LedgerCommand ledgerCommand;

    // ── LedgerAccount ────────────────────────────────────────────

    @Override
    public LedgerPayload.AccountResponse createAccount(LedgerPayload.CreateAccountRequest request) {
        Long id = ledgerCommand.createAccount(new CreateLedgerAccountCommand(
            request.accountCode(), request.name(), request.category(),
            request.currency(), request.parentId(), request.description()
        ));
        return getAccount(id);
    }

    @Override
    public LedgerPayload.AccountResponse getAccount(Long id) {
        return ledgerReader.findAccountById(id)
            .map(this::toAccountResponse)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "LedgerAccount id=" + id));
    }

    @Override
    public ApiPageResponse<LedgerPayload.AccountResponse> searchAccounts(
            Pageable pageable, LedgerPayload.AccountSearchRequest request) {
        LedgerAccountSearchCondition condition = LedgerAccountSearchCondition.builder()
            .accountCode(request.accountCode())
            .name(request.name())
            .category(request.category())
            .parentId(request.parentId())
            .currency(request.currency())
            .isActive(request.isActive())
            .build();
        Page<LedgerAccountResult> page = ledgerReader.searchAccounts(pageable, condition);
        return ApiPageResponse.of(
            page.getContent().stream().map(this::toAccountResponse).toList(),
            page.getTotalElements(), page.getNumber(), page.getSize()
        );
    }

    @Override
    public void renameAccount(Long id, LedgerPayload.RenameAccountRequest request) {
        ledgerCommand.renameAccount(id, request.name(), request.description());
    }

    @Override
    public void discontinueAccount(Long id) {
        ledgerCommand.discontinueAccount(id);
    }

    // ── AccountingPeriod ─────────────────────────────────────────

    @Override
    public LedgerPayload.PeriodResponse openPeriod(LedgerPayload.OpenPeriodRequest request) {
        Long id = ledgerCommand.openPeriod(new OpenPeriodCommand(
            request.fiscalYear(), request.fiscalPeriod(), request.startDate(), request.endDate()
        ));
        return getPeriod(id);
    }

    @Override
    public LedgerPayload.PeriodResponse getPeriod(Long id) {
        return ledgerReader.findPeriodById(id)
            .map(this::toPeriodResponse)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "AccountingPeriod id=" + id));
    }

    @Override
    public void closePeriod(Long id, LedgerPayload.ClosePeriodRequest request) {
        ledgerCommand.closePeriod(id, request.closedByAdminId());
    }

    // ── JournalEntry ─────────────────────────────────────────────

    @Override
    public LedgerPayload.JournalResponse postJournal(LedgerPayload.PostJournalRequest request) {
        List<JournalLineInput> lines = request.lines().stream()
            .map(l -> new JournalLineInput(
                l.ledgerAccountId(), l.debit(), l.credit(), l.currency(), l.description()
            )).toList();

        Long id = ledgerCommand.createAndPostJournal(new PostJournalEntryCommand(
            request.journalNumber(), request.entryDate(), request.description(),
            request.referenceType(), request.referenceId(), lines
        ));
        return getJournal(id);
    }

    @Override
    public LedgerPayload.JournalResponse getJournal(Long id) {
        return ledgerReader.findEntryById(id)
            .map(this::toJournalResponse)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "JournalEntry id=" + id));
    }

    @Override
    public ApiPageResponse<LedgerPayload.JournalResponse> searchJournals(
            Pageable pageable, LedgerPayload.JournalSearchRequest request) {
        JournalEntrySearchCondition condition = JournalEntrySearchCondition.builder()
            .journalNumber(request.journalNumber())
            .periodId(request.periodId())
            .status(request.status())
            .referenceType(request.referenceType())
            .referenceId(request.referenceId())
            .entryDateFrom(request.entryDateFrom())
            .entryDateTo(request.entryDateTo())
            .build();
        Page<JournalEntryResult> page = ledgerReader.searchEntries(pageable, condition);
        return ApiPageResponse.of(
            page.getContent().stream().map(this::toJournalResponse).toList(),
            page.getTotalElements(), page.getNumber(), page.getSize()
        );
    }

    @Override
    public LedgerPayload.JournalResponse reverseJournal(Long id, LedgerPayload.ReverseJournalRequest request) {
        Long reversalId = ledgerCommand.reverseJournal(id, request.reversalJournalNumber(), request.reason());
        return getJournal(reversalId);
    }

    // ── Reports ──────────────────────────────────────────────────

    @Override
    public List<LedgerPayload.TrialBalanceLine> trialBalance(LocalDate from, LocalDate to) {
        return ledgerReader.trialBalance(from, to).stream().map(this::toTrialBalanceLine).toList();
    }

    // ── Mapper ────────────────────────────────────────────────────

    private LedgerPayload.AccountResponse toAccountResponse(LedgerAccountResult r) {
        return new LedgerPayload.AccountResponse(
            r.id(), r.accountCode(), r.name(), r.category(), r.normalBalance(),
            r.parentId(), r.currency(), r.isActive(), r.description()
        );
    }

    private LedgerPayload.PeriodResponse toPeriodResponse(AccountingPeriodResult r) {
        return new LedgerPayload.PeriodResponse(
            r.id(), r.fiscalYear(), r.fiscalPeriod(), r.startDate(), r.endDate(),
            r.status(), r.closedAt(), r.closedByAdminId()
        );
    }

    private LedgerPayload.JournalResponse toJournalResponse(JournalEntryResult r) {
        List<LedgerPayload.JournalLineResponse> lines = r.lines().stream()
            .map(this::toJournalLineResponse).toList();
        return new LedgerPayload.JournalResponse(
            r.id(), r.journalNumber(), r.entryDate(), r.periodId(),
            r.description(), r.referenceType(), r.referenceId(),
            r.status(), r.postedAt(), r.reversedByJournalId(), r.reversesJournalId(),
            r.totalDebit(), r.totalCredit(), lines
        );
    }

    private LedgerPayload.JournalLineResponse toJournalLineResponse(JournalLineResult r) {
        return new LedgerPayload.JournalLineResponse(
            r.id(), r.ledgerAccountId(), r.debit(), r.credit(), r.currency(), r.description()
        );
    }

    private LedgerPayload.TrialBalanceLine toTrialBalanceLine(TrialBalanceLine r) {
        return new LedgerPayload.TrialBalanceLine(
            r.ledgerAccountId(), r.accountCode(), r.accountName(), r.category(),
            r.normalBalance(), r.totalDebit(), r.totalCredit(), r.balance()
        );
    }
}
