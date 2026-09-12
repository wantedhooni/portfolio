package com.revy.example.order.client.dto

data class OrderResponse(val orderId: Long, val product: Product, val quantity: Int, val total: Int)