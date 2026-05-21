package com.revy.example.trade.reader.dto;

import com.revy.example.domain.account.enums.TxStatus;
import com.revy.example.domain.account.enums.TxType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
public class TradeSearchCondition {

    Long      accountId;
    Long      stockId;
    TxType    txType;      // null = BUY + SELL 전체
    TxStatus  status;
    String    referenceId;
    Instant   tradedAtFrom;
    Instant   tradedAtTo;

    @Builder
    public TradeSearchCondition(Long accountId, Long stockId, TxType txType, TxStatus status,
                                String referenceId, Instant tradedAtFrom, Instant tradedAtTo) {
        this.accountId    = accountId;
        this.stockId      = stockId;
        this.txType       = txType;
        this.status       = status;
        this.referenceId  = referenceId;
        this.tradedAtFrom = tradedAtFrom;
        this.tradedAtTo   = tradedAtTo;
    }
}
