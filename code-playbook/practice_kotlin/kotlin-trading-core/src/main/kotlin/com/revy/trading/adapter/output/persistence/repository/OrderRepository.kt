package com.revy.trading.adapter.output.persistence.repository

import com.revy.trading.adapter.output.persistence.entity.OrderEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OrderRepository : JpaRepository<OrderEntity, UUID> {

    fun findByOrderId(orderId: UUID): OrderEntity?
}