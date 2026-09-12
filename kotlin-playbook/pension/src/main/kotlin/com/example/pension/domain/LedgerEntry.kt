package com.example.pension.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "ledger_entry",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_ledger_transaction_sequence",
            columnNames = ["transaction_id", "sequence"]
        )
    ]
)
class LedgerEntry protected constructor(
    @Column(name = "account_id", nullable = false, updatable = false)
    val accountId: Long,

    @Column(name = "transaction_id", nullable = false, updatable = false)
    val transactionId: UUID,

    @Column(name = "sequence", nullable = false, updatable = false)
    val sequence: Int,

    @Column(name = "event_type", nullable = false, updatable = false, length = 50)
    val eventType: String,

    @Column(name = "product_code", length = 50, updatable = false)
    val productCode: String?,

    @Column(name = "cash_delta", nullable = false, precision = 19, scale = 2, updatable = false)
    val cashDelta: BigDecimal,

    @Column(name = "quantity_delta", nullable = false, precision = 24, scale = 8, updatable = false)
    val quantityDelta: BigDecimal,

    @Column(name = "occurred_at", nullable = false, updatable = false)
    val occurredAt: Instant,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    companion object {
        fun cash(
            accountId: Long,
            transactionId: UUID,
            sequence: Int,
            eventType: String,
            cashDelta: BigDecimal,
            occurredAt: Instant = Instant.now(),
        ) = LedgerEntry(
            accountId = accountId,
            transactionId = transactionId,
            sequence = sequence,
            eventType = eventType,
            productCode = null,
            cashDelta = cashDelta,
            quantityDelta = BigDecimal.ZERO.setScale(8),
            occurredAt = occurredAt,
            createdAt = Instant.now(),
        )

        fun buyExecution(
            accountId: Long,
            transactionId: UUID,
            sequence: Int,
            productCode: String,
            cashDelta: BigDecimal,
            quantityDelta: BigDecimal,
            occurredAt: Instant,
        ) = LedgerEntry(
            accountId = accountId,
            transactionId = transactionId,
            sequence = sequence,
            eventType = "ORDER_BUY_FILLED",
            productCode = productCode,
            cashDelta = cashDelta,
            quantityDelta = quantityDelta,
            occurredAt = occurredAt,
            createdAt = Instant.now(),
        )
    }
}