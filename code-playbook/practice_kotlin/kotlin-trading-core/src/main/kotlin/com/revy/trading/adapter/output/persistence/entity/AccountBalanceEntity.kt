package com.revy.trading.adapter.output.persistence.entity

import com.revy.trading.adapter.output.persistence.entity.common.VersionedEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(
    name = "account_balance",
    indexes = [Index(
        name = "idx_balance_account_id",
        columnList = "account_id",
        unique = true,
    )],

    )
class AccountBalanceEntity(
    @Column(name = "account_id", nullable = false, unique = true, updatable = false, columnDefinition = "uuid")
    val accountId: UUID,
    @Column(name = "available_amount", nullable = false, precision = 19, scale = 6)
    var availableAmount: BigDecimal,
    @Column(name = "reserved_amount", nullable = false, precision = 19, scale = 6)
    var reservedAmount: BigDecimal,
    @Column(nullable = false, length = 3)
    val currency: String,
    ) : VersionedEntity() {}