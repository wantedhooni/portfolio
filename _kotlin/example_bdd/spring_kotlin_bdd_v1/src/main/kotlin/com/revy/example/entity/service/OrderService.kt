package com.revy.example.entity.service

import com.revy.example.entity.Order
import com.revy.example.entity.enums.OrderStatus
import com.revy.example.entity.repository.OrderRepository
import com.revy.example.entity.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal


@Service
@Transactional(readOnly = true)
class OrderService(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
) {

    @Transactional
    fun placeOrder(customerId: Long, request: PlaceOrderRequest): OrderResult {
        val order = Order(customerId = customerId)

        request.items.forEach { item ->
            val product = productRepository.findByIdWithLock(item.productId)
                ?: throw NoSuchElementException("상품을 찾을 수 없습니다. id=${item.productId}")
            order.addItem(product, item.quantity)
        }

        return orderRepository.save(order).toResult()
    }

    @Transactional
    fun confirmOrder(orderId: Long): OrderResult {
        val order = orderRepository.findByIdWithItems(orderId)
            ?: throw NoSuchElementException("주문을 찾을 수 없습니다. id=$orderId")
        order.confirm()
        return order.toResult()
    }

    @Transactional
    fun cancelOrder(orderId: Long): OrderResult {
        val order = orderRepository.findByIdWithItems(orderId)
            ?: throw NoSuchElementException("주문을 찾을 수 없습니다. id=$orderId")
        order.cancel()
        return order.toResult()
    }

    fun getOrder(orderId: Long): OrderResult =
        orderRepository.findByIdWithItems(orderId)
            ?.toResult()
            ?: throw NoSuchElementException("주문을 찾을 수 없습니다. id=$orderId")
}

// DTO
data class PlaceOrderRequest(
    val items: List<OrderItemRequest>,
)

data class OrderItemRequest(
    val productId: Long,
    val quantity: Int,
)

data class OrderResult(
    val orderId: Long,
    val status: OrderStatus,
    val totalPrice: BigDecimal,
    val itemCount: Int,
)

fun Order.toResult() = OrderResult(
    orderId = id,
    status = status,
    totalPrice = totalPrice,
    itemCount = items.size,
)