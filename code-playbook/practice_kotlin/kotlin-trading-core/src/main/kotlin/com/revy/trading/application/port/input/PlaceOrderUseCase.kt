package com.revy.trading.application.port.input

import com.revy.trading.application.port.input.payload.PlaceOrderCommand
import com.revy.trading.application.port.input.payload.PlaceOrderResult

interface PlaceOrderUseCase {
    fun place(command: PlaceOrderCommand): PlaceOrderResult
}