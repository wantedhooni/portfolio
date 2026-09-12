package com.revy.trading.domain

import com.revy.trading.domain.common.Money
import com.revy.trading.domain.enums.OrderSide
import com.revy.trading.domain.enums.OrderStatus
import com.revy.trading.domain.order.Order
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.util.UUID

class OrderBehaviorSpec : BehaviorSpec({
    Given("승인된 10주 매수 주문이 있을 때") {
        val order = Order.create(
            accountId = UUID.randomUUID(),
            symbol = "AAPL",
            side = OrderSide.BUY,
            quantity = 10,
            limitPrice = Money(BigDecimal("200"), "USD"),
        ).apply { accept() }

        When("주문 수량보다 많이 체결하면") {
            val exception = shouldThrow<IllegalStateException> {
                order.fill(11, Money(BigDecimal("199"), "USD"))
            }

            Then("과다 체결이 거부되고 주문 상태는 유지된다") {
                exception.message shouldBe "over fill detected"
                order.status() shouldBe OrderStatus.ACCEPTED
                order.filledQuantity() shouldBe 0
            }
        }
    }
})
