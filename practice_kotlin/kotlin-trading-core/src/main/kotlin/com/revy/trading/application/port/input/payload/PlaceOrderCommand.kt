package com.revy.trading.application.port.input.payload

import com.revy.trading.domain.enums.OrderSide
import java.math.BigDecimal
import java.util.UUID

data class PlaceOrderCommand(
    val accountId: UUID,
    val symbol: String,
    val side: OrderSide,
    val quantity: Long,
    val limitPrice: BigDecimal,
)
