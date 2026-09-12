package com.revy.trading.application.port.input.payload


import com.revy.trading.domain.enums.OrderStatus
import java.util.UUID

data class PlaceOrderResult(
    val orderId: UUID,
    val status: OrderStatus,
)