package com.revy.example.domain.ledger.enums;

import com.revy.example.common.enums.ExposedEnum;

public enum AccountCategory implements ExposedEnum {
    ASSET(NormalBalance.DEBIT),       // 자산   (차변잔액)
    LIABILITY(NormalBalance.CREDIT),  // 부채   (대변잔액)
    EQUITY(NormalBalance.CREDIT),     // 자본   (대변잔액)
    REVENUE(NormalBalance.CREDIT),    // 수익   (대변잔액)
    EXPENSE(NormalBalance.DEBIT);     // 비용   (차변잔액)

    private final NormalBalance normalBalance;

    AccountCategory(NormalBalance normalBalance) {
        this.normalBalance = normalBalance;
    }

    public NormalBalance getNormalBalance() {
        return normalBalance;
    }
}
