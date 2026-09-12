package com.revy.trading.adapter.output.persistence.mapper

import com.revy.trading.adapter.output.persistence.entity.OrderEntity
import com.revy.trading.domain.common.Money
import com.revy.trading.domain.order.Order

fun OrderEntity.toDomain(): Order =
    Order.restore(
        orderId = orderId,
        accountId = accountId,
        symbol = symbol,
        side = side,
        quantity = quantity,
        limitPrice = Money(
            amount = limitPrice,
            currency = currency,
        ),
        status = status,
        filledQuantity = filledQuantity,
    )

fun Order.toEntity(): OrderEntity =
    OrderEntity(
        orderId = orderId,
        accountId = accountId,
        symbol = symbol,
        side = side,
        quantity = quantity,
        limitPrice = limitPrice.amount,
        currency = limitPrice.currency,
        status = status(),
        filledQuantity = filledQuantity(),
    )
