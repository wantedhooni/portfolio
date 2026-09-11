package com.example.pension.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.persistence.Version
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(
    name = "holding",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_holding_account_product",
            columnNames = ["account_id", "product_code"]
        )
    ]
)
class Holding protected constructor(
    @Column(name = "account_id", nullable = false, updatable = false)
    val accountId: Long,

    @Column(name = "product_code", nullable = false, updatable = false, length = 50)
    val productCode: String,

    @Column(name = "quantity", nullable = false, precision = 24, scale = 8)
    var quantity: BigDecimal,

    @Column(name = "book_value", nullable = false, precision = 19, scale = 2)
    var bookValue: BigDecimal,

    @Column(name = "market_value", nullable = false, precision = 19, scale = 2)
    var marketValue: BigDecimal,

    @Column(name = "valuation_date")
    var valuationDate: LocalDate?,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    @Version
    var version: Long = 0
        protected set

    fun buy(executedAmount: BigDecimal, executedQuantity: BigDecimal) {
        require(executedAmount > BigDecimal.ZERO)
        require(executedQuantity > BigDecimal.ZERO)
        quantity = quantity.add(executedQuantity)
        bookValue = bookValue.add(executedAmount)
        require(quantity >= BigDecimal.ZERO)
        require(bookValue >= BigDecimal.ZERO)
    }

    companion object {
        fun empty(accountId: Long, productCode: String) =
            Holding(
                accountId = accountId,
                productCode = productCode,
                quantity = BigDecimal.ZERO.setScale(8),
                bookValue = BigDecimal.ZERO.setScale(2),
                marketValue = BigDecimal.ZERO.setScale(2),
                valuationDate = null,
            )
    }
}