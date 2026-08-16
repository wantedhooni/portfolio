package com.revy.trading.domain.balance

import com.revy.trading.domain.common.Money
import java.util.UUID

class AccountBalance(
    val accountId: UUID,
    private var available: Money,
    private var reserved: Money,
) {
    fun reserve(amount: Money) {
        require(amount.amount.signum() > 0)
        require(available.currency == amount.currency)
        check(available.amount >= amount.amount) {
            "insufficient balance"
        }
        available -= amount
        reserved += amount
    }

    fun release(amount: Money) {
        require(amount.amount.signum() > 0)
        require(reserved.currency == amount.currency)

        check(reserved.amount >= amount.amount) {
            "insufficient reserved balance"
        }

        available += amount
        reserved -= amount
    }

    fun available(): Money = available

    fun reserved(): Money = reserved
}