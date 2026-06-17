package com.revy.example.entity.securities

import com.revy.example.entity.common.BaseEntity
import com.revy.example.entity.securities.enums.TradeAction
import com.revy.example.entity.securities.enums.TransactionType
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
@Table(name = "TB_CUSTOMER_TRANSACTION")
class CustomerTransaction protected constructor(

    @Column(name = "TRANSACTION_NO", length = 30)
    val transactionNo: String,

    @Column(name = "TRANSACTION_DATE", nullable = false)
    val transactionDate: LocalDate,

    // 거래주체번호 (FK) - 계약정보 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONTRACT_NO", nullable = false)
    val contract: Contract,

    // 상품코드 (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_CODE", nullable = false)
    val product: Product,

    @Enumerated(EnumType.STRING)
    @Column(name = "TRANSACTION_TYPE", nullable = false, length = 30)
    val transactionType: TransactionType,

    @Enumerated(EnumType.STRING)
    @Column(name = "TRADE_ACTION", nullable = false, length = 10)
    val tradeAction: TradeAction,

    @Column(name = "TRANSACTION_MEDIA", length = 20)
    var transactionMedia: String?, // 거래매체

    @Column(name = "ORDER_MEDIA", length = 20)
    var orderMedia: String?, // 주문매체

    // 일정정보(p) - 매수일/체결일/만기일
    @Column(name = "BUY_DATE")
    var buyDate: LocalDate?,

    @Column(name = "SETTLE_DATE")
    var settleDate: LocalDate?,

    @Column(name = "MATURITY_DATE")
    var maturityDate: LocalDate?

) : BaseEntity() {
    companion object {
        fun of(
            transactionNo: String,
            transactionDate: LocalDate,
            contract: Contract,
            product: Product,
            transactionType: TransactionType,
            tradeAction: TradeAction,
            transactionMedia: String? = null,
            orderMedia: String? = null,
            buyDate: LocalDate? = null,
            settleDate: LocalDate? = null,
            maturityDate: LocalDate? = null
        ): CustomerTransaction = CustomerTransaction(
            transactionNo, transactionDate, contract, product, transactionType, tradeAction,
            transactionMedia, orderMedia, buyDate, settleDate, maturityDate
        )
    }
}