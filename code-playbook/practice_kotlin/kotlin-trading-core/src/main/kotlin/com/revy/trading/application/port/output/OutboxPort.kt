package com.revy.trading.application.port.output

import java.util.UUID

interface OutboxPort {
    fun append(
        aggregateId: UUID,
        eventType: String,
        payload: String,
    )
}
