package com.revy.example.entity

import com.revy.example.entity.common.BaseEntity
import com.revy.example.entity.enums.OrderStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "orders")
class Order(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id", nullable = false)
    var mamber: Member,


    @Column(name="total_price", nullable=false)
    var totalPrice: BigDecimal = BigDecimal.ZERO,

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable=false)
    var status: OrderStatus = OrderStatus.PENDING

) : BaseEntity(){
    var orderItem: MutableList<OrderItem> = mutableListOf()

    fun addOrderItem(item: OrderItem) {
        orderItem.add(item)
        //item.ass
        this.totalPrice = this.totalPrice.add(BigDecimal(100))
    }
}