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

@Entity
@Table(
    name = "order_execution",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_execution_external_execution_id",
            columnNames = ["external_execution_id"]
        )
    ]
)
class OrderExecution protected constructor(
    @Column(name = "order_id", nullable = false, updatable = false)
    val orderId: Long,

    @Column(name = "external_execution_id", nullable = false, updatable = false, length = 100)
    val externalExecutionId: String,

    @Column(name = "executed_amount", nullable = false, precision = 19, scale = 2)
    val executedAmount: BigDecimal,

    @Column(name = "quantity", nullable = false, precision = 24, scale = 8)
    val quantity: BigDecimal,

    @Column(name = "executed_at", nullable = false, updatable = false)
    val executedAt: Instant,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    companion object {
        fun create(
            orderId: Long,
            externalExecutionId: String,
            executedAmount: BigDecimal,
            quantity: BigDecimal,
            executedAt: Instant,
        ) = OrderExecution(
            orderId,
            externalExecutionId,
            executedAmount,
            quantity,
            executedAt,
        )
    }
}