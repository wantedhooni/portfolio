package com.revy.trading

import com.revy.trading.domain.balance.AccountBalance
import com.revy.trading.domain.common.Money
import com.revy.trading.domain.enums.OrderSide
import com.revy.trading.domain.enums.OrderStatus
import com.revy.trading.domain.order.Order
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import kotlin.test.assertEquals

class TradingStepDefinitions {
    private lateinit var balance: AccountBalance
    private lateinit var order: Order
    private lateinit var concurrentResults: List<Boolean>

    @Given("가용 잔고가 {long} USD이다")
    fun givenAvailableBalance(amount: Long) {
        balance = AccountBalance(
            accountId = UUID.randomUUID(),
            available = money(amount),
            reserved = money(0),
        )
    }

    @When("{long} USD를 예약한다")
    fun reserveBalance(amount: Long) {
        balance.reserve(money(amount))
    }

    @Then("가용 잔고는 {long} USD다")
    fun verifyAvailableBalance(amount: Long) {
        assertEquals(BigDecimal.valueOf(amount), balance.available().amount)
    }

    @Then("예약 잔고는 {long} USD다")
    fun verifyReservedBalance(amount: Long) {
        assertEquals(BigDecimal.valueOf(amount), balance.reserved().amount)
    }

    @Given("{long}주 매수 주문이 CREATED 상태다")
    fun givenCreatedOrder(quantity: Long) {
        order = createOrder(quantity)
    }

    @Given("{long}주 매수 주문이 ACCEPTED 상태다")
    fun givenAcceptedOrder(quantity: Long) {
        order = createOrder(quantity).apply { accept() }
    }

    @When("주문을 승인한다")
    fun acceptOrder() {
        order.accept()
    }

    @When("{long}주를 {long} USD에 체결한다")
    fun fillOrder(quantity: Long, price: Long) {
        order.fill(quantity, money(price))
    }

    @Then("주문 상태는 {word}다")
    fun verifyOrderStatus(status: String) {
        assertEquals(OrderStatus.valueOf(status), order.status())
    }

    @Then("누적 체결 수량은 {long}주다")
    fun verifyFilledQuantity(quantity: Long) {
        assertEquals(quantity, order.filledQuantity())
    }

    @When("두 주문이 동시에 {long} USD씩 예약한다")
    fun reserveBalanceConcurrently(amount: Long) {
        val executor = Executors.newFixedThreadPool(2)
        val barrier = CyclicBarrier(2)

        try {
            concurrentResults = (1..2).map {
                executor.submit<Boolean> {
                    barrier.await()
                    runCatching {
                        synchronized(balance) {
                            balance.reserve(money(amount))
                        }
                    }.isSuccess
                }
            }.map { it.get() }
        } finally {
            executor.shutdownNow()
        }
    }

    @Then("성공한 예약은 {int}건이다")
    fun verifySuccessfulReservations(count: Int) {
        assertEquals(count, concurrentResults.count { it })
    }

    private fun createOrder(quantity: Long): Order = Order.create(
        accountId = UUID.randomUUID(),
        symbol = "AAPL",
        side = OrderSide.BUY,
        quantity = quantity,
        limitPrice = money(200),
    )

    private fun money(amount: Long): Money = Money(BigDecimal.valueOf(amount), "USD")
}
