package com.revy.example.ledger.command.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * 분개 생성 + 즉시 POST.
 * 다른 도메인(Insurance/FX/Account)에서 자동 분개 생성 시 사용.
 *
 * 주의: lines의 차변 합 = 대변 합 이어야 post() 성공
 */
public record PostJournalEntryCommand(
        String journalNumber,
        LocalDate entryDate,
        String description,
        String referenceType,
        String referenceId,
        List<JournalLineInput> lines
) {}
