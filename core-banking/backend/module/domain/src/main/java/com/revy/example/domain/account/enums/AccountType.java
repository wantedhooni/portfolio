package com.revy.example.domain.account.enums;

import com.revy.example.common.enums.ExposedEnum;

public enum AccountType implements ExposedEnum {
    REAL,       // 실제 증권 계좌
    VIRTUAL     // 가상(모의투자) 계좌
}