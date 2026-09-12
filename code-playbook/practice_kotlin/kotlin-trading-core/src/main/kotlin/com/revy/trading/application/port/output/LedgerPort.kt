package com.revy.trading.application.port.output

import com.revy.trading.domain.ledger.LedgerTransaction

interface LedgerPort {
    fun append(transaction: LedgerTransaction)
}