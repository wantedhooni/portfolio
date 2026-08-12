package com.revy.example.domain

import com.revy.example.domain.common.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "orders")
class Order(
    @Column(name="order_date", nullable = false, columnDefinition ="TIMESTAMP")
    var orderDate: Instant = Instant.now(),

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable = false)
    var status: OrderStatus = OrderStatus.PENDING,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    val orderItems: MutableList<OrderItem> = mutableListOf()

) : BaseEntity() {


    fun addOrderItem(item: OrderItem){
        item.order = this
        orderItems.add(item)
    }

    override fun toString(): String {
        return "Order(orderDate=$orderDate, status=$status, orderItems=$orderItems) ${super.toString()}"
    }

}


@Entity
@Table(name = "order_item")
class OrderItem(
    @Column(name="product_name", nullable = false)
    var productName: String,

    @Column(name="quantity", nullable = false)
    var quantity: Int,

    @Column(name="price", nullable = false)
    var price: BigDecimal,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    var order: Order? = null

): BaseEntity() {
    override fun toString(): String {
        return "OrderItem(productName='$productName', quantity=$quantity, price=$price, orderId=$order.id) ${super.toString()}"
    }

}

enum class OrderStatus {
    PENDING, PAID, SHIPPED, CANCELLED
}