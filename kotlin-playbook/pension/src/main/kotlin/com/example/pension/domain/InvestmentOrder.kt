package com.example.pension.domain

import com.example.pension.domain.shared.OrderSide
import com.example.pension.domain.shared.OrderStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.persistence.Version
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(
    name = "investment_order",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_order_client_order_id", columnNames = ["client_order_id"])
    ]
)
class InvestmentOrder protected constructor(
    @Column(name = "account_id", nullable = false, updatable = false)
    val accountId: Long,

    @Column(name = "client_order_id", nullable = false, updatable = false, length = 100)
    val clientOrderId: String,

    @Column(name = "product_code", nullable = false, updatable = false, length = 50)
    val productCode: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false, updatable = false, length = 10)
    val side: OrderSide,

    @Column(name = "requested_amount", nullable = false, precision = 19, scale = 2)
    val requestedAmount: BigDecimal,

    @Column(name = "filled_amount", nullable = false, precision = 19, scale = 2)
    var filledAmount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    var status: OrderStatus,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    @Version
    var version: Long = 0
        protected set

    val remainingAmount: BigDecimal
        get() = requestedAmount.subtract(filledAmount)

    fun fill(amount: BigDecimal) {
        require(amount > BigDecimal.ZERO)
        require(status in setOf(
            OrderStatus.REQUESTED,
            OrderStatus.ACCEPTED,
            OrderStatus.PARTIALLY_FILLED
        ))
        require(amount <= remainingAmount) { "fill exceeds remaining amount" }

        filledAmount = filledAmount.add(amount)
        status = if (filledAmount.compareTo(requestedAmount) == 0) {
            OrderStatus.FILLED
        } else {
            OrderStatus.PARTIALLY_FILLED
        }
    }

    fun cancel(): BigDecimal {
        require(status in setOf(
            OrderStatus.REQUESTED,
            OrderStatus.ACCEPTED,
            OrderStatus.PARTIALLY_FILLED
        ))
        val releasable = remainingAmount
        status = OrderStatus.CANCELLED
        return releasable
    }

    companion object {
        fun buy(
            accountId: Long,
            clientOrderId: String,
            productCode: String,
            amount: BigDecimal,
        ): InvestmentOrder {
            require(amount > BigDecimal.ZERO)
            return InvestmentOrder(
                accountId = accountId,
                clientOrderId = clientOrderId,
                productCode = productCode,
                side = OrderSide.BUY,
                requestedAmount = amount,
                filledAmount = BigDecimal.ZERO.setScale(2),
                status = OrderStatus.REQUESTED,
                createdAt = Instant.now(),
            )
        }
    }
}