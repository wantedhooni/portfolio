package com.revy.securities.domain.processor.account.dto;

import com.revy.common.enums.Currency;
import com.revy.securities.domain.account.enums.AccountType;

import java.math.BigDecimal;

public class AccountResultDto {

    public record AccountInfo(
            AccountType type,
            String accountNo,
            Currency currency,
            BigDecimal cashBalance,
            BigDecimal availableCash
    ) {

    }
}
