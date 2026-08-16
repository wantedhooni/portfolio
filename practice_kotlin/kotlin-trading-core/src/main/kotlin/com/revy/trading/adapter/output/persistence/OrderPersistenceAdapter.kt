package com.revy.trading.adapter.output.persistence

import com.revy.trading.adapter.output.persistence.mapper.toDomain
import com.revy.trading.adapter.output.persistence.mapper.toEntity
import com.revy.trading.adapter.output.persistence.repository.OrderRepository
import com.revy.trading.application.port.output.OrderPort
import com.revy.trading.domain.order.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
class OrderPersistenceAdapter(
    private val repository: OrderRepository
) : OrderPort {

    @Transactional(readOnly = true)
    override fun findByOrderId(orderId: UUID): Order? {
        return repository.findByOrderId(orderId)?.toDomain()
    }

    @Transactional
    override fun save(order: Order) {
        val entity = repository.findByOrderId(order.orderId)

        if (entity == null) {
            repository.save(order.toEntity())
            return
        }

        entity.status = order.status()
        entity.filledQuantity = order.filledQuantity()
    }
}
