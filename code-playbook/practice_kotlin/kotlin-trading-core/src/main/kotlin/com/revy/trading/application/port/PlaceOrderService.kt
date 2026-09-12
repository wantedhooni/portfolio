package com.revy.trading.application.port


import com.revy.trading.application.port.input.PlaceOrderUseCase
import com.revy.trading.application.port.input.payload.PlaceOrderCommand
import com.revy.trading.application.port.input.payload.PlaceOrderResult
import com.revy.trading.application.port.output.*
import com.revy.trading.domain.common.Money
import com.revy.trading.domain.enums.LedgerAccountType
import com.revy.trading.domain.enums.LedgerDirection
import com.revy.trading.domain.enums.OrderSide
import com.revy.trading.domain.ledger.LedgerEntry
import com.revy.trading.domain.ledger.LedgerTransaction
import com.revy.trading.domain.order.Order
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

@Service
class PlaceOrderService(
    private val objectMapper: ObjectMapper,
    private val orderPort: OrderPort,
    private val balancePort: BalancePort,
    private val ledgerPort: LedgerPort,
    private val outboxPort: OutboxPort,
) : PlaceOrderUseCase {

    @Transactional
    override fun place(command: PlaceOrderCommand): PlaceOrderResult {
        val limitPrice = Money(
            amount = command.limitPrice,
            currency = "USD",
        )

        val order = Order.create(
            accountId = command.accountId,
            symbol = command.symbol,
            side = command.side,
            quantity = command.quantity,
            limitPrice = limitPrice,
        )
        if (command.side == OrderSide.BUY) {
            reserveBalance(order)
        }

        order.accept()
        orderPort.save(order)

        order.pullDomainEvents().forEach { event ->
            outboxPort.append(
                aggregateId = order.orderId,
                eventType = event.javaClass.simpleName,
                payload = objectMapper.writeValueAsString(event),
            )
        }
        return PlaceOrderResult(
            orderId = order.orderId,
            status = order.status(),
        )
    }

    private fun reserveBalance(order: Order) {
        val reserveAmount = order.limitPrice * order.quantity

        val balance = balancePort.findByAccountId(order.accountId)
        balance.reserve(reserveAmount)
        balancePort.save(balance)

        ledgerPort.append(
            LedgerTransaction(
                referenceId = order.orderId,
                entries = listOf(
                    LedgerEntry(
                        accountId = order.accountId,
                        ledgerAccountType = LedgerAccountType.RESERVED_CASH,
                        direction = LedgerDirection.DEBIT,
                        amount = reserveAmount,
                    ),
                    LedgerEntry(
                        accountId = order.accountId,
                        ledgerAccountType = LedgerAccountType.AVAILABLE_CASH,
                        direction = LedgerDirection.CREDIT,
                        amount = reserveAmount,
                    ),
                ),
            )
        )
    }

}
