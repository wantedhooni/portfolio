package com.revy.example.entity

import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.Column
import jakarta.persistence.Id
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import java.math.BigDecimal

@Entity
@Table(name = "products")
class Product(
    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val price: BigDecimal,

    @Column(nullable = false)
    var stock: Int,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
) {
    fun decreaseStock(quantity: Int) {
        require(stock >= quantity) { "재고 부족" }
        stock -= quantity
    }

    fun increaseStock(quantity: Int) {
        stock += quantity
    }
}