package com.revy.example.account.reader.dto;

import com.revy.example.domain.account.enums.TxStatus;
import com.revy.example.domain.account.enums.TxType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
public class AccountTxSearchCondition {

    Long id;
    Long accountId;
    Long stockId;
    TxType txType;
    TxStatus status;
    String referenceId;
    Instant tradedAtFrom;
    Instant tradedAtTo;

    @Builder
    public AccountTxSearchCondition(Long id, Long accountId, Long stockId,
                                    TxType txType, TxStatus status, String referenceId,
                                    Instant tradedAtFrom, Instant tradedAtTo) {
        this.id           = id;
        this.accountId    = accountId;
        this.stockId      = stockId;
        this.txType       = txType;
        this.status       = status;
        this.referenceId  = referenceId;
        this.tradedAtFrom = tradedAtFrom;
        this.tradedAtTo   = tradedAtTo;
    }
}
