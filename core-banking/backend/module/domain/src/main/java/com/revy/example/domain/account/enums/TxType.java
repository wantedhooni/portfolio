package com.revy.example.domain.account.enums;

import com.revy.example.common.enums.ExposedEnum;

public enum TxType implements ExposedEnum {
    DEPOSIT,        // 현금 입금
    WITHDRAWAL,     // 현금 출금
    TRANSFER_OUT,   // 계좌이체 (출금측)
    TRANSFER_IN,    // 계좌이체 (입금측)
    BUY,            // 주식 매수
    SELL,           // 주식 매도
    DIVIDEND,       // 배당금 수령
    FEE,            // 거래 수수료 (별도 청구분)
    TAX             // 세금 (거래세·배당소득세 등)
}