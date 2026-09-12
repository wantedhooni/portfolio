package com.revy.trading.adapter.output.persistence.mapper

import com.revy.trading.adapter.output.persistence.entity.LedgerEntryEntity
import com.revy.trading.domain.ledger.LedgerTransaction

fun LedgerTransaction.toEntities(): List<LedgerEntryEntity> = entries.map { entry ->
    LedgerEntryEntity(
        transactionId = transactionId,
        referenceId = referenceId,
        accountId = entry.accountId,
        ledgerAccountType = entry.ledgerAccountType,
        direction = entry.direction,
        amount = entry.amount.amount,
        currency = entry.amount.currency,
    )
}
