package com.revy.example.account.reader.dto;


import com.revy.example.domain.account.enums.TxStatus;
import com.revy.example.domain.account.enums.TxType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@NoArgsConstructor
@Getter
public class AccountTxSearchCondition {
    Long id;
    Long accountId;
    Long stockId;
    TxType txType;
    TxStatus status;
    String referenceId;
    Instant tradedAt;

    @Builder
    public AccountTxSearchCondition(Long id, Long accountId, Long stockId, TxType txType, TxStatus status,
                                    String referenceId, Instant tradedAt) {
        this.id = id;
        this.accountId = accountId;
        this.stockId = stockId;
        this.txType = txType;
        this.status = status;
        this.referenceId = referenceId;
        this.tradedAt = tradedAt;
    }
}
