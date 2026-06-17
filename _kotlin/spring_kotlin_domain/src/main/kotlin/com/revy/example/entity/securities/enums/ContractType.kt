package com.revy.example.entity.securities.enums

enum class ContractType {

    // 일반계약
    CORPORATE_LOAN,      // 기업대출
    FINANCING,           // 파이넨싱
    IPO,                 // IPO
    MA,                  // M&A
    CARD_CONTRACT,       // 카드계약
    CORPORATE_ADVISORY,  // 기업자문

    // 자사계약
    OVERSEAS_ACCOUNT,    // 해외계좌
    LONG_TERM_BORROWING, // 장기차입
    IPO_UNDERWRITING,    // 공모주인수
    ASSET_TRADING,       // 자산매매
    ASSET_LEASING,       // 자산임대차

    // 계좌계약
    COMPREHENSIVE_TRUST_DEPOSIT, // 종합매매-위탁형
    FUTURES_OPTIONS,             // 선물,옵션
    SINGLE_TRADE_SAVINGS,        // 단일매매-저축형
    SINGLE_TRADE_TRUST           // 단일매매-신탁형
}