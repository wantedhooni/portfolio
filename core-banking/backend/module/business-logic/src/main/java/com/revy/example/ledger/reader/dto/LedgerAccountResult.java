package com.revy.example.ledger.reader.dto;

import com.revy.example.domain.ledger.LedgerAccount;
import com.revy.example.domain.ledger.enums.AccountCategory;
import com.revy.example.domain.ledger.enums.NormalBalance;

public record LedgerAccountResult(
        Long id,
        String accountCode,
        String name,
        AccountCategory category,
        NormalBalance normalBalance,
        Long parentId,
        String currency,
        boolean isActive,
        String description
) {
    public static LedgerAccountResult from(LedgerAccount a) {
        return new LedgerAccountResult(
            a.getId(), a.getAccountCode(), a.getName(),
            a.getCategory(), a.getNormalBalance(), a.getParentId(),
            a.getCurrency(), a.isActive(), a.getDescription()
        );
    }
}
