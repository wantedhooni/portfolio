package com.revy.example

import com.revy.example.entity.Product
import com.revy.example.entity.repository.OrderRepository
import com.revy.example.entity.repository.ProductRepository
import com.revy.example.entity.service.OrderItemRequest
import com.revy.example.entity.service.OrderService
import com.revy.example.entity.service.PlaceOrderRequest
import io.cucumber.datatable.DataTable
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.assertj.core.api.Assertions.assertThat
import java.math.BigDecimal

class OrderSteps(
    private val orderService: OrderService,
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
) {

    private val productMap = mutableMapOf<String, Product>()
    private var currentOrderId: Long = 0L
    private var caughtException: Exception? = null

    // ── Background ──────────────────────────────────────────

    @Given("다음 상품이 등록되어 있다")
    fun givenProducts(dataTable: DataTable) {
        productMap.clear()
        currentOrderId = 0L
        caughtException = null

        dataTable.asMaps().forEach { row ->
            val product = productRepository.save(
                Product(
                    name = row["name"]!!,
                    price = row["price"]!!.toBigDecimal(),
                    stock = row["stock"]!!.toInt(),
                )
            )

            productMap[product.name] = product
        }
    }

    // ── Given ───────────────────────────────────────────────

    @Given("고객\\({long})이 노트북 {int}개를 주문했다")
    fun givenOrderWithLaptop(customerId: Long, qty: Int) {
        placeSingleProductOrder(customerId, "노트북", qty)
    }

    @Given("고객\\({long})이 마우스 {int}개를 주문했다")
    fun givenOrderWithMouse(customerId: Long, qty: Int) {
        placeSingleProductOrder(customerId, "마우스", qty)
    }

    // ── When ────────────────────────────────────────────────

    @When("고객\\({long})이 다음 상품을 주문한다")
    fun whenPlaceOrder(customerId: Long, dataTable: DataTable) {
        caughtException = null

        val items = dataTable.asMaps().map { row ->
            val productName = row["productName"]!!
            val product = productMap[productName]
                ?: error("테스트 데이터에 없는 상품: $productName")

            OrderItemRequest(
                productId = product.id,
                quantity = row["quantity"]!!.toInt(),
            )
        }

        runCatching {
            val result = orderService.placeOrder(
                customerId,
                PlaceOrderRequest(items),
            )
            currentOrderId = result.orderId
        }.onFailure {
            caughtException = it as? Exception
        }
    }

    @When("주문을 확정한다")
    fun whenConfirmOrder() {
        caughtException = null

        runCatching {
            orderService.confirmOrder(currentOrderId)
        }.onFailure {
            caughtException = it as? Exception
        }
    }

    @When("주문을 다시 확정 시도한다")
    fun whenConfirmOrderAgain() {
        whenConfirmOrder()
    }

    @When("주문을 취소한다")
    fun whenCancelOrder() {
        caughtException = null

        runCatching {
            orderService.cancelOrder(currentOrderId)
        }.onFailure {
            caughtException = it as? Exception
        }
    }

    // ── Then ────────────────────────────────────────────────

    @Then("주문 상태는 {string} 이어야 한다")
    fun thenOrderStatus(expectedStatus: String) {
        val order = orderService.getOrder(currentOrderId)

        assertThat(order.status.name).isEqualTo(expectedStatus)
    }

    @Then("주문 총액은 {long} 원 이어야 한다")
    fun thenTotalPrice(expectedPrice: Long) {
        val order = orderService.getOrder(currentOrderId)

        assertThat(order.totalPrice)
            .isEqualByComparingTo(BigDecimal.valueOf(expectedPrice))
    }

    @Then("{word} 재고는 {int} 개 이어야 한다")
    fun thenProductStock(productName: String, expectedStock: Int) {
        val productId = productMap[productName]?.id
            ?: error("테스트 데이터에 없는 상품: $productName")

        val product = productRepository.findById(productId).get()

        assertThat(product.stock).isEqualTo(expectedStock)
    }

    @Then("주문은 실패하고 {string} 메시지가 반환되어야 한다")
    fun thenOrderFailed(expectedMessage: String) {
        assertThat(caughtException).isNotNull
        assertThat(caughtException!!.message).contains(expectedMessage)
    }

    @Then("{string} 오류가 발생해야 한다")
    fun thenExceptionWithMessage(expectedMessage: String) {
        assertThat(caughtException).isNotNull
        assertThat(caughtException!!.message).contains(expectedMessage)
    }

    @Then("{word} 재고는 {int} 개로 복구되어야 한다")
    fun thenStockRestored(productName: String, expectedStock: Int) {
        thenProductStock(productName, expectedStock)
    }

    // ── Helper ──────────────────────────────────────────────

    private fun placeSingleProductOrder(
        customerId: Long,
        productName: String,
        quantity: Int,
    ) {
        caughtException = null

        val product = productMap[productName]
            ?: error("$productName 상품 없음")

        val result = orderService.placeOrder(
            customerId,
            PlaceOrderRequest(
                listOf(
                    OrderItemRequest(
                        productId = product.id,
                        quantity = quantity,
                    )
                )
            )
        )

        currentOrderId = result.orderId
    }
}