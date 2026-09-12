package com.revy.trading.domain.order.event

import com.revy.trading.domain.enums.OrderSide
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class OrderCreatedEvent(
    override val orderId: UUID,
    val accountId: UUID,
    val symbol: String,
    val side: OrderSide,
    val quantity: Long,
    val limitPrice: BigDecimal,
    override val occurredAt: Instant = Instant.now(),
) : OrderEvent {
}