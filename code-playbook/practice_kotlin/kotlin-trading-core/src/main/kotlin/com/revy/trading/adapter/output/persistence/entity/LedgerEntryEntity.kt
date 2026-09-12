package com.revy.trading.adapter.output.persistence.entity

import com.revy.trading.adapter.output.persistence.entity.common.BaseEntity
import com.revy.trading.domain.enums.LedgerAccountType
import com.revy.trading.domain.enums.LedgerDirection
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(
    name = "ledger_entry",
    indexes = [
        Index(
            name = "idx_ledger_transaction_id",
            columnList = "transaction_id",
        ),
        Index(
            name = "idx_ledger_account_id",
            columnList = "account_id",
        ),
        Index(
            name = "idx_ledger_reference_id",
            columnList = "reference_id",
        ),
    ],
)
class LedgerEntryEntity(
    @Column(name = "transaction_id", nullable = false, updatable = false, columnDefinition = "uuid")
    val transactionId: UUID,

    @Column(name = "reference_id", nullable = false, updatable = false, columnDefinition = "uuid")
    val referenceId: UUID,

    @Column(name = "account_id", nullable = false, updatable = false, columnDefinition = "uuid")
    val accountId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "ledger_account_type", nullable = false, updatable = false)
    val ledgerAccountType: LedgerAccountType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    val direction: LedgerDirection,

    @Column(nullable = false, precision = 19, scale = 6, updatable = false)
    val amount: BigDecimal,

    @Column(nullable = false, length = 3, updatable = false)
    val currency: String,
) : BaseEntity() {}