package com.revy.securities.domain.processor.account;

import com.revy.common.enums.Currency;
import com.revy.securities.domain.account.enums.AccountType;
import com.revy.securities.domain.processor.account.dto.AccountResultDto;
import org.springframework.transaction.annotation.Transactional;

public interface AccountProcessor {
    @Transactional
    AccountResultDto.AccountInfo create(Long userId, AccountType accountType, Currency currency);
}
