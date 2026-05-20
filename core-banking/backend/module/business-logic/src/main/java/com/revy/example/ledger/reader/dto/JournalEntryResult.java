package com.revy.example.ledger.reader.dto;

import com.revy.example.domain.ledger.JournalEntry;
import com.revy.example.domain.ledger.enums.JournalStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record JournalEntryResult(
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
        List<JournalLineResult> lines
) {
    public static JournalEntryResult from(JournalEntry e) {
        return new JournalEntryResult(
            e.getId(), e.getJournalNumber(), e.getEntryDate(), e.getPeriodId(),
            e.getDescription(), e.getReferenceType(), e.getReferenceId(),
            e.getStatus(), e.getPostedAt(),
            e.getReversedByJournalId(), e.getReversesJournalId(),
            e.totalDebit(), e.totalCredit(),
            e.getLines().stream().map(JournalLineResult::from).toList()
        );
    }
}
