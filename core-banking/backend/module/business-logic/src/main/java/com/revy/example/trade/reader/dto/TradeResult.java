package com.revy.example.trade.reader.dto;

import com.revy.example.domain.account.AccountTx;
import com.revy.example.domain.account.enums.TxStatus;
import com.revy.example.domain.account.enums.TxType;

import java.math.BigDecimal;
import java.time.Instant;

public record TradeResult(
        Long       id,
        Long       accountId,
        Long       stockId,
        TxType     txType,
        BigDecimal quantity,
        BigDecimal price,
        BigDecimal amount,
        BigDecimal fee,
        BigDecimal tax,
        TxStatus   status,
        String     referenceId,
        Instant    tradedAt
) {

    public static TradeResult from(AccountTx tx) {
        return new TradeResult(
            tx.getId(),
            tx.getAccountId(),
            tx.getStockId(),
            tx.getTxType(),
            tx.getQuantity(),
            tx.getPrice(),
            tx.getAmount(),
            tx.getFee(),
            tx.getTax(),
            tx.getStatus(),
            tx.getReferenceId(),
            tx.getTradedAt()
        );
    }
}
