package com.revy.example.ledger.reader.dto;

import com.revy.example.domain.ledger.enums.AccountCategory;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LedgerAccountSearchCondition {

    String accountCode;
    String name;
    AccountCategory category;
    Long parentId;
    String currency;
    Boolean isActive;

    @Builder
    public LedgerAccountSearchCondition(String accountCode, String name, AccountCategory category,
                                        Long parentId, String currency, Boolean isActive) {
        this.accountCode  = accountCode;
        this.name         = name;
        this.category     = category;
        this.parentId     = parentId;
        this.currency     = currency;
        this.isActive     = isActive;
    }
}
