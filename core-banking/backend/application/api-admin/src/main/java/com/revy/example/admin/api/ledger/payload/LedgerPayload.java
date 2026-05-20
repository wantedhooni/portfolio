package com.revy.example.admin.api.ledger.payload;

import com.revy.example.domain.ledger.enums.AccountCategory;
import com.revy.example.domain.ledger.enums.JournalStatus;
import com.revy.example.domain.ledger.enums.NormalBalance;
import com.revy.example.domain.ledger.enums.PeriodStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class LedgerPayload {

    // ── LedgerAccount ────────────────────────────────────────────

    @Schema(name = "LedgerPayload.CreateAccountRequest")
    public record CreateAccountRequest(
            @NotBlank @Size(max = 20) String accountCode,
            @NotBlank String name,
            @NotNull AccountCategory category,
            @NotBlank @Size(min = 3, max = 3) String currency,
            Long parentId,
            String description
    ) {}

    @Schema(name = "LedgerPayload.RenameAccountRequest")
    public record RenameAccountRequest(
            @NotBlank String name,
            String description
    ) {}

    @Schema(name = "LedgerPayload.AccountSearchRequest")
    public record AccountSearchRequest(
            String accountCode,
            String name,
            AccountCategory category,
            Long parentId,
            String currency,
            Boolean isActive
    ) {}

    @Schema(name = "LedgerPayload.AccountResponse")
    public record AccountResponse(
            Long id,
            String accountCode,
            String name,
            AccountCategory category,
            NormalBalance normalBalance,
            Long parentId,
            String currency,
            boolean isActive,
            String description
    ) {}

    // ── AccountingPeriod ─────────────────────────────────────────

    @Schema(name = "LedgerPayload.OpenPeriodRequest")
    public record OpenPeriodRequest(
            @NotNull @Min(2000) @Max(2999) Integer fiscalYear,
            @NotNull @Min(1) @Max(12) Integer fiscalPeriod,
            @NotNull LocalDate startDate,
            @NotNull LocalDate endDate
    ) {}

    @Schema(name = "LedgerPayload.ClosePeriodRequest")
    public record ClosePeriodRequest(
            @NotNull Long closedByAdminId
    ) {}

    @Schema(name = "LedgerPayload.PeriodResponse")
    public record PeriodResponse(
            Long id,
            Integer fiscalYear,
            Integer fiscalPeriod,
            LocalDate startDate,
            LocalDate endDate,
            PeriodStatus status,
            Instant closedAt,
            Long closedByAdminId
    ) {}

    // ── JournalEntry ─────────────────────────────────────────────

    @Schema(name = "LedgerPayload.PostJournalRequest")
    public record PostJournalRequest(
            @NotBlank String journalNumber,
            @NotNull LocalDate entryDate,
            @NotBlank String description,
            String referenceType,
            String referenceId,
            @NotEmpty List<JournalLineRequest> lines
    ) {}

    @Schema(name = "LedgerPayload.JournalLineRequest")
    public record JournalLineRequest(
            @NotNull Long ledgerAccountId,
            @DecimalMin("0.0") BigDecimal debit,
            @DecimalMin("0.0") BigDecimal credit,
            @NotBlank String currency,
            String description
    ) {}

    @Schema(name = "LedgerPayload.ReverseJournalRequest")
    public record ReverseJournalRequest(
            @NotBlank String reversalJournalNumber,
            @NotBlank String reason
    ) {}

    @Schema(name = "LedgerPayload.JournalSearchRequest")
    public record JournalSearchRequest(
            String journalNumber,
            Long periodId,
            JournalStatus status,
            String referenceType,
            String referenceId,
            LocalDate entryDateFrom,
            LocalDate entryDateTo
    ) {}

    @Schema(name = "LedgerPayload.JournalResponse")
    public record JournalResponse(
            Long id,
            String journalNumber,
            LocalDate entryDate,
            Long periodId,
            String description,
            String referenceType,
            String referenceId,
            JournalStatus status,
            Instant postedAt,
            Long reversedByJournalId,
            Long reversesJournalId,
            BigDecimal totalDebit,
            BigDecimal totalCredit,
            List<JournalLineResponse> lines
    ) {}

    @Schema(name = "LedgerPayload.JournalLineResponse")
    public record JournalLineResponse(
            Long id,
            Long ledgerAccountId,
            BigDecimal debit,
            BigDecimal credit,
            String currency,
            String description
    ) {}

    // ── Trial Balance ────────────────────────────────────────────

    @Schema(name = "LedgerPayload.TrialBalanceLine")
    public record TrialBalanceLine(
            Long ledgerAccountId,
            String accountCode,
            String accountName,
            AccountCategory category,
            NormalBalance normalBalance,
            BigDecimal totalDebit,
            BigDecimal totalCredit,
            BigDecimal balance
    ) {}
}
