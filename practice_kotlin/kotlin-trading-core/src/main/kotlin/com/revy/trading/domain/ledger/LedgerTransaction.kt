package com.revy.trading.domain.ledger

import com.revy.trading.domain.enums.LedgerDirection
import com.revy.trading.utils.UuidUtil
import java.time.Instant
import java.util.UUID

data class LedgerTransaction(
    val transactionId: UUID = UuidUtil.generateUuidV7(),
    val referenceId: UUID,
    val entries: List<LedgerEntry>,
    val occurredAt: Instant = Instant.now(),
) {
    init {
        require(entries.isNotEmpty())

        val currencies = entries.map { it.amount.currency }.distinct()

        require(currencies.size == 1) {
            "multiple currencies not allowed"
        }

        val debit = entries.filter { it.direction == LedgerDirection.DEBIT }.sumOf { it.amount.amount }

        val credit = entries.filter { it.direction == LedgerDirection.CREDIT }.sumOf { it.amount.amount }

        require(debit.compareTo(credit) == 0) {
            "unbalanced ledger transaction"
        }
    }
}