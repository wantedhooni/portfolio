package com.revy.trading.application.port.output

import com.revy.trading.domain.balance.AccountBalance
import java.util.UUID

interface BalancePort {
    fun findByAccountId(accountId: UUID): AccountBalance
    fun save(balance: AccountBalance)
}