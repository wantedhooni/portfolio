package com.revy.example.order.controller

import com.revy.example.order.client.ProductClient
import com.revy.example.order.client.dto.OrderResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
class OrderController(private val productClient: ProductClient) {

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): OrderResponse {
        // 데모: 주문 id를 productId로 간주, 수량 2 고정
        val product = productClient.getProduct(id)
        return OrderResponse(
            orderId = id,
            product = product,
            quantity = 2,
            total = product.price * 2,
        )
    }
}