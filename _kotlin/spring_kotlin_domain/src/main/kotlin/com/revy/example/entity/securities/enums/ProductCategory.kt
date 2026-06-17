package com.revy.example.entity.securities.enums

enum class ProductCategory {

    // 판매상품
    SAVINGS, CMA, TRUST, LOAN, PENSION,

    // 종목
    STOCK, OVERSEAS_STOCK, EQUITY_LINKED_SECURITY, ETF,   // 주식형
    BOND, COMMERCIAL_PAPER, CD, DLS,                       // 채권형
    MUTUAL_FUND, BENEFICIARY_CERTIFICATE,                  // 수익증권형
    OVERSEAS_FORWARD, OPTION, ELW,                         // 파생상품형

    // 서비스
    INSURANCE_SERVICE, AGENCY_SERVICE, ADDITIONAL_SERVICE
}