package com.revy.example.entity.securities

import com.revy.example.entity.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "TB_STOCK_TRADE_DETAIL")
class StockTradeDetail protected constructor(

    @Column(name = "TRANSACTION_NO", length = 30)
    val transactionNo: String,

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "TRANSACTION_NO")
    val transaction: CustomerTransaction,

    @Column(name = "CREDIT_TYPE", length = 10)
    var creditType: String?, // 신용구분

    @Column(name = "MARKET_TRADE_TYPE", length = 10)
    var marketTradeType: String? // 시장거래구분

) : BaseEntity() {
    companion object {
        fun of(transaction: CustomerTransaction, creditType: String?, marketTradeType: String?): StockTradeDetail =
            StockTradeDetail(transaction.transactionNo, transaction, creditType, marketTradeType)
    }
}