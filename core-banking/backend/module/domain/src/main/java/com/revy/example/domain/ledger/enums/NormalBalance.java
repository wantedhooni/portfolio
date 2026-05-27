package com.revy.example.domain.ledger.enums;

import com.revy.example.common.enums.ExposedEnum;

public enum NormalBalance implements ExposedEnum {
    DEBIT,    // 차변잔액 (자산·비용)
    CREDIT    // 대변잔액 (부채·자본·수익)
}
