package com.revy.example.entity.repository

import com.revy.example.entity.Order
import com.revy.example.entity.Product
import com.revy.example.entity.enums.OrderStatus
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

// OrderRepository.kt
@Repository
interface OrderRepository : JpaRepository<Order, Long> {

    fun findAllByCustomerId(customerId: Long): List<Order>

    @Query("""
        SELECT o FROM Order o
        JOIN FETCH o.items i
        JOIN FETCH i.product
        WHERE o.id = :orderId
    """)
    fun findByIdWithItems(@Param("orderId") orderId: Long): Order?

    @Query("""
        SELECT o FROM Order o
        WHERE o.status = :status
          AND o.createdAt BETWEEN :from AND :to
    """)
    fun findByStatusAndPeriod(
        @Param("status") status: OrderStatus,
        @Param("from") from: LocalDateTime,
        @Param("to") to: LocalDateTime,
    ): List<Order>
}


