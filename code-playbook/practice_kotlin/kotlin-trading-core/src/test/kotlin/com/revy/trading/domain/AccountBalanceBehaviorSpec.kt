package com.revy.trading.domain

import com.revy.trading.domain.balance.AccountBalance
import com.revy.trading.domain.common.Money
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.util.UUID

class AccountBalanceBehaviorSpec : BehaviorSpec({
    Given("가용 잔고가 10,000 USD인 계정이 있을 때") {
        val balance = AccountBalance(
            accountId = UUID.randomUUID(),
            available = Money(BigDecimal("10000"), "USD"),
            reserved = Money(BigDecimal.ZERO, "USD"),
        )

        When("잔고보다 큰 금액을 예약하면") {
            val exception = shouldThrow<IllegalStateException> {
                balance.reserve(Money(BigDecimal("10001"), "USD"))
            }

            Then("예약이 거부되고 잔고는 변하지 않는다") {
                exception.message shouldBe "insufficient balance"
                balance.available().amount shouldBe BigDecimal("10000")
                balance.reserved().amount shouldBe BigDecimal.ZERO
            }
        }
    }
})
