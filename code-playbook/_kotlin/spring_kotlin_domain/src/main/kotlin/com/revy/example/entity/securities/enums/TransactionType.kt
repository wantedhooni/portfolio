package com.revy.example.entity.securities.enums

enum class TransactionType {
    PRODUCT_TRADE,        // 상품거래
    FINANCIAL_TRADE,      // 금융상품매매
    SECURITIES_TRADE,     // 수익증권거래
    FUTURES_OPTION_TRADE, // 선물옵션거래
    TRUST_TRADE,          // 신탁거래
    STOCK_TRADE,          // 주식거래
    OVERSEAS_TRADE        // 해외투자거래
}
