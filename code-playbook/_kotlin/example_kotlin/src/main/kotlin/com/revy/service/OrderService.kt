package com.revy.service

import com.revy.domain.entity.Order
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service

@Service
class OrderService(
    val entityManager: EntityManager
) {

    fun createOrder(): Order {
        var newOrder = Order();
        entityManager.persist(newOrder)
        return newOrder;
    }

}
