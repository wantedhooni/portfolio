package com.revy.example.entity.repository

import com.revy.example.entity.OrderItem
import org.springframework.data.jpa.repository.JpaRepository

interface OrderItemRepository : JpaRepository<OrderItem, Long> {
}