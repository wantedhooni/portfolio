package com.revy.trading.domain.common

import java.math.BigDecimal

data class Money(
    val amount: BigDecimal,
    val currency: String,
) {
    init {
        require(currency.isNotBlank()) { "Currency must not be blank." }
    }

    operator fun plus(other: Money): Money {
        requireSameCurrency(other)
        return copy(amount = amount + other.amount)
    }

    operator fun minus(other: Money): Money {
        requireSameCurrency(other)
        return copy(amount = amount - other.amount)
    }

    operator fun times(quantity: Long): Money {
        require(quantity > 0)

        return copy(
            amount = amount.multiply(quantity.toBigDecimal())
        )
    }


    private fun requireSameCurrency(other: Money) {
        require(currency == other.currency) {
            "currency mismatch: $currency != ${other.currency}"
        }
    }
}
