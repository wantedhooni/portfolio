package com.revy.trading.adapter.output.persistence.entity

import com.revy.trading.adapter.output.persistence.entity.common.VersionedEntity
import com.revy.trading.domain.enums.OrderSide
import com.revy.trading.domain.enums.OrderStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = "orders")
class OrderEntity(
    @Column(name = "order_id", nullable = false, unique = true, updatable = false, columnDefinition = "uuid")
    val orderId: UUID,

    @Column(name = "account_id", nullable = false, updatable = false, columnDefinition = "uuid")
    val accountId: UUID,

    @Column(nullable = false, updatable = false, length = 20)
    val symbol: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 10)
    val side: OrderSide,

    @Column(nullable = false, updatable = false)
    val quantity: Long,

    @Column(name = "limit_price", nullable = false, precision = 19, scale = 6, updatable = false)
    val limitPrice: BigDecimal,

    @Column(nullable = false, length = 3, updatable = false)
    val currency: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: OrderStatus,

    @Column(name = "filled_quantity", nullable = false)
    var filledQuantity: Long,

    ) : VersionedEntity() {

}