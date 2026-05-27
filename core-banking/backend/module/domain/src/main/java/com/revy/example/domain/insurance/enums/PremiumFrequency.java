package com.revy.example.domain.insurance.enums;

import com.revy.example.common.enums.ExposedEnum;
public enum PremiumFrequency implements ExposedEnum{
    MONTHLY,      // 월납
    QUARTERLY,    // 분기납
    SEMIANNUAL,   // 반기납
    ANNUAL,       // 연납
    ONE_TIME      // 일시납
}
