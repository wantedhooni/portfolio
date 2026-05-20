package com.revy.example.ledger.reader.dto;

import com.revy.example.domain.ledger.enums.JournalStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class JournalEntrySearchCondition {

    String journalNumber;
    Long periodId;
    JournalStatus status;
    String referenceType;
    String referenceId;
    LocalDate entryDateFrom;
    LocalDate entryDateTo;

    @Builder
    public JournalEntrySearchCondition(String journalNumber, Long periodId, JournalStatus status,
                                       String referenceType, String referenceId,
                                       LocalDate entryDateFrom, LocalDate entryDateTo) {
        this.journalNumber  = journalNumber;
        this.periodId       = periodId;
        this.status         = status;
        this.referenceType  = referenceType;
        this.referenceId    = referenceId;
        this.entryDateFrom  = entryDateFrom;
        this.entryDateTo    = entryDateTo;
    }
}
