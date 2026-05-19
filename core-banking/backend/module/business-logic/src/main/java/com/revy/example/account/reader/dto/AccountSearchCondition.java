package com.revy.example.account.reader.dto;

import com.revy.example.domain.account.enums.AccountStatus;
import com.revy.example.domain.account.enums.AccountType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AccountSearchCondition {

    Long userId;
    String accountNumber;
    AccountType accountType;
    AccountStatus status;
    String currency;

    @Builder
    public AccountSearchCondition(Long userId, String accountNumber,
                                  AccountType accountType, AccountStatus status,
                                  String currency) {
        this.userId        = userId;
        this.accountNumber = accountNumber;
        this.accountType   = accountType;
        this.status        = status;
        this.currency      = currency;
    }
}
