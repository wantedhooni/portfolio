package com.revy.securities.domain.account.enums;


public enum AccountType {
    CASH,   // 현금계좌: 정산된 현금 범위 내에서만 거래(일반적 정의)
    MARGIN  // 마진계좌: 브로커로부터 차입하여 매수가능금액이 확대될 수 있음
}