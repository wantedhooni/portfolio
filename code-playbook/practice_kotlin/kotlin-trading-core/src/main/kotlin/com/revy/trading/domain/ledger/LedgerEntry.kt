package com.revy.trading.domain.ledger

import com.revy.trading.domain.common.Money
import com.revy.trading.domain.enums.LedgerAccountType
import com.revy.trading.domain.enums.LedgerDirection
import java.util.UUID

data class LedgerEntry(
    val accountId: UUID,
    val ledgerAccountType: LedgerAccountType,
    val direction: LedgerDirection,
    val amount: Money,
)