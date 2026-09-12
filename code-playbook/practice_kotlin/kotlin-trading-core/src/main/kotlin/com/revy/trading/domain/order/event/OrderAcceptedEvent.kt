package com.revy.trading.domain.order.event

import java.time.Instant
import java.util.UUID

data class OrderAcceptedEvent(
    override val orderId: UUID,
    override val occurredAt: Instant = Instant.now(),
) : OrderEvent