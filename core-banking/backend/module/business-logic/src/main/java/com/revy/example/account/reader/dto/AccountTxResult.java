package com.revy.example.account.reader.dto;

import com.revy.example.domain.account.AccountTx;
import com.revy.example.domain.account.enums.TxStatus;
import com.revy.example.domain.account.enums.TxType;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountTxResult(
        Long id,
        Long accountId,
        Long stockId,
        TxType txType,
        BigDecimal amount,
        BigDecimal quantity,
        BigDecimal price,
        BigDecimal fee,
        BigDecimal tax,
        TxStatus status,
        String referenceId,
        Instant tradedAt
) {

    public static AccountTxResult from(AccountTx tx) {
        return new AccountTxResult(
            tx.getId(),
            tx.getAccountId(),
            tx.getStockId(),
            tx.getTxType(),
            tx.getAmount(),
            tx.getQuantity(),
            tx.getPrice(),
            tx.getFee(),
            tx.getTax(),
            tx.getStatus(),
            tx.getReferenceId(),
            tx.getTradedAt()
        );
    }
}
