package com.revy.trading.domain.order


import com.revy.trading.domain.common.Money
import com.revy.trading.domain.enums.OrderSide
import com.revy.trading.domain.enums.OrderStatus
import com.revy.trading.domain.order.event.OrderAcceptedEvent
import com.revy.trading.domain.order.event.OrderCreatedEvent
import com.revy.trading.domain.order.event.OrderEvent
import com.revy.trading.domain.order.event.OrderFilledEvent
import java.util.*

class Order private constructor(
    val orderId: UUID,
    val accountId: UUID,
    val symbol: String,
    val side: OrderSide,
    val quantity: Long,
    val limitPrice: Money,
    private var status: OrderStatus,
    private var filledQuantity: Long,
) {

    private val domainEvents = mutableListOf<OrderEvent>()

    companion object {

        fun create(
            accountId: UUID,
            symbol: String,
            side: OrderSide,
            quantity: Long,
            limitPrice: Money,
        ): Order {
            require(symbol.isNotBlank())
            require(quantity > 0)
            require(limitPrice.amount.signum() > 0)

            val order = Order(
                orderId = UUID.randomUUID(),
                accountId = accountId,
                symbol = symbol,
                side = side,
                quantity = quantity,
                limitPrice = limitPrice,
                status = OrderStatus.CREATED,
                filledQuantity = 0,
            )

            order.domainEvents += OrderCreatedEvent(
                orderId = order.orderId,
                accountId = accountId,
                symbol = symbol,
                side = side,
                quantity = quantity,
                limitPrice = limitPrice.amount,
            )

            return order
        }

        fun restore(
            orderId: UUID,
            accountId: UUID,
            symbol: String,
            side: OrderSide,
            quantity: Long,
            limitPrice: Money,
            status: OrderStatus,
            filledQuantity: Long,
        ): Order = Order(
            orderId = orderId,
            accountId = accountId,
            symbol = symbol,
            side = side,
            quantity = quantity,
            limitPrice = limitPrice,
            status = status,
            filledQuantity = filledQuantity,
        )
    }

    fun accept() {
        check(status == OrderStatus.CREATED) {
            "invalid transition: $status -> ACCEPTED"
        }

        status = OrderStatus.ACCEPTED
        domainEvents += OrderAcceptedEvent(orderId)
    }

    fun fill(
        quantity: Long,
        price: Money,
    ) {
        require(quantity > 0)
        require(price.currency == limitPrice.currency)

        check(
            status == OrderStatus.ACCEPTED || status == OrderStatus.PARTIALLY_FILLED
        ) {
            "order cannot be filled: status=$status"
        }

        val nextFilledQuantity = filledQuantity + quantity

        check(nextFilledQuantity <= this.quantity) {
            "over fill detected"
        }

        filledQuantity = nextFilledQuantity
        status = if (filledQuantity == this.quantity) {
            OrderStatus.FILLED
        } else {
            OrderStatus.PARTIALLY_FILLED
        }

        domainEvents += OrderFilledEvent(
            orderId = orderId,
            fillQuantity = quantity,
            fillPrice = price.amount,
        )
    }

    fun cancel() {
        check(
            status == OrderStatus.ACCEPTED || status == OrderStatus.PARTIALLY_FILLED
        ) {
            "order cannot be canceled: status=$status"
        }

        status = OrderStatus.CANCELED
    }

    fun status(): OrderStatus = status

    fun filledQuantity(): Long = filledQuantity

    fun pullDomainEvents(): List<OrderEvent> = domainEvents.toList().also { domainEvents.clear() }
}