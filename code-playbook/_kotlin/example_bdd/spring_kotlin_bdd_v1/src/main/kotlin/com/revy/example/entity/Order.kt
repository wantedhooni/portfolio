package com.revy.example.entity

import com.revy.example.entity.enums.OrderStatus
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
class Order(
    @Column
    val customerId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.PENDING,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    val items: MutableList<OrderItem> = mutableListOf(),

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
) {
    // 비즈니스 메서드
    fun addItem(product: Product, quantity: Int) {
        require(quantity > 0) { "수량은 1개 이상이어야 합니다." }
        require(product.stock >= quantity) { "재고가 부족합니다. 현재 재고: ${product.stock}" }

        items.add(OrderItem(order = this, product = product, quantity = quantity))
        product.decreaseStock(quantity)
    }

    fun confirm() {
        check(status == OrderStatus.PENDING) { "대기 중인 주문만 확정할 수 있습니다." }
        status = OrderStatus.CONFIRMED
    }

    fun cancel() {
        check(status != OrderStatus.DELIVERED) { "배송 완료된 주문은 취소할 수 없습니다." }
        items.forEach { it.product.increaseStock(it.quantity) }
        status = OrderStatus.CANCELLED
    }

    val totalPrice: BigDecimal
        get() = items.sumOf { it.subtotal }
}