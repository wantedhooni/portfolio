package com.revy.trading.controller

import com.revy.trading.application.port.input.PlaceOrderUseCase
import com.revy.trading.application.port.input.payload.PlaceOrderCommand
import com.revy.trading.domain.enums.OrderSide
import com.revy.trading.domain.enums.OrderStatus
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.util.UUID

@RestController
@RequestMapping("/orders")
class OrderController(
    private val placeOrderUseCase: PlaceOrderUseCase,
) {

    @PostMapping
    fun place(
        @RequestBody
        request: PlaceOrderRequest,
    ): PlaceOrderResponse {
        val result = placeOrderUseCase.place(
            PlaceOrderCommand(
                accountId = request.accountId,
                symbol = request.symbol,
                side = request.side,
                quantity = request.quantity,
                limitPrice = request.limitPrice,
            )
        )

        return PlaceOrderResponse(
            orderId = result.orderId,
            status = result.status,
        )
    }
}

data class PlaceOrderRequest(
    val accountId: UUID,
    val symbol: String,
    val side: OrderSide,
    val quantity: Long,
    val limitPrice: BigDecimal,
)

data class PlaceOrderResponse(
    val orderId: UUID,
    val status: OrderStatus,
)