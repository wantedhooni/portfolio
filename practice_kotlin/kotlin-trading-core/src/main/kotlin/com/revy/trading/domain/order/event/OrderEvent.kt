package com.revy.trading.domain.order.event

import java.time.Instant
import java.util.UUID

interface OrderEvent {
    val orderId: UUID
    val occurredAt: Instant
}