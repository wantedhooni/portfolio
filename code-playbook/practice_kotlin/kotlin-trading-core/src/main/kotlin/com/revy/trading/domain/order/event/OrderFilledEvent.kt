package com.revy.trading.domain.order.event

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class OrderFilledEvent(
    override val orderId: UUID,
    val fillQuantity: Long,
    val fillPrice: BigDecimal,
    override val occurredAt: Instant = Instant.now(),
): OrderEvent
