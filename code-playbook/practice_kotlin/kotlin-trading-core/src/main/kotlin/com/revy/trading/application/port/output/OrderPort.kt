package com.revy.trading.application.port.output

import com.revy.trading.domain.order.Order
import java.util.*

interface OrderPort {
    fun findByOrderId(orderId: UUID): Order?

    fun save(order: Order)
}