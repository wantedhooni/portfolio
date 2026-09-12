package com.revy.example.entity.securities


import com.revy.example.entity.common.BaseEntity
import com.revy.example.entity.securities.enums.OrderStatus
import com.revy.example.entity.securities.enums.OrderTradeAction
import com.revy.example.entity.securities.enums.OrderTradeCategory
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "TB_CUSTOMER_ORDER_EXECUTION")
class CustomerOrderExecution protected constructor(

    @Column(name = "ORDER_EXECUTION_NO", length = 30)
    val orderExecutionNo: String,

    @Column(name = "ORDER_EXECUTION_DATE", nullable = false)
    val orderExecutionDate: LocalDate,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONTRACT_NO", nullable = false)
    val contract: Contract,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_CODE", nullable = false)
    val product: Product,

    @Enumerated(EnumType.STRING)
    @Column(name = "TRADE_ACTION", nullable = false, length = 10)
    var tradeAction: OrderTradeAction, // 거래형태: 매도/매수

    @Enumerated(EnumType.STRING)
    @Column(name = "ORDER_STATUS", nullable = false, length = 10)
    var orderStatus: OrderStatus, // 거래상태: 신청/체결/결제

    @Enumerated(EnumType.STRING)
    @Column(name = "TRADE_CATEGORY", nullable = false, length = 30)
    val tradeCategory: OrderTradeCategory // 거래구분

): BaseEntity() {
    companion object {
        fun of(
            orderExecutionNo: String,
            orderExecutionDate: LocalDate,
            contract: Contract,
            product: Product,
            tradeAction: OrderTradeAction,
            orderStatus: OrderStatus,
            tradeCategory: OrderTradeCategory
        ): CustomerOrderExecution = CustomerOrderExecution(
            orderExecutionNo, orderExecutionDate, contract, product,
            tradeAction, orderStatus, tradeCategory
        )

        /** 주문 최초 등록은 항상 '신청(주문)' 상태로 시작 */
        fun request(
            orderExecutionNo: String,
            orderExecutionDate: LocalDate,
            contract: Contract,
            product: Product,
            tradeAction: OrderTradeAction,
            tradeCategory: OrderTradeCategory
        ): CustomerOrderExecution = of(
            orderExecutionNo, orderExecutionDate, contract, product,
            tradeAction, OrderStatus.REQUESTED, tradeCategory
        )
    }

    fun settle() {
        check(orderStatus == OrderStatus.REQUESTED) { "신청 상태에서만 체결 처리할 수 있습니다." }
        orderStatus = OrderStatus.SETTLED
    }

    fun pay() {
        check(orderStatus == OrderStatus.SETTLED) { "체결 상태에서만 결제 처리할 수 있습니다." }
        orderStatus = OrderStatus.PAID
    }
}