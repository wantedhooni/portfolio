package com.revy.example.entity

import com.revy.example.entity.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "order_items")
class OrderItem(
    @Column(name = "product_name", nullable = false)
    var productName: String,
    @Column(name = "price", nullable = false)
    var price: BigDecimal,
    @Column(name = "quantity", nullable = false)
    var quantity: Int,
) : BaseEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    var order: Order? = null
        protected set

    fun assignOrder(order: Order) {
        this.order = order
    }


}