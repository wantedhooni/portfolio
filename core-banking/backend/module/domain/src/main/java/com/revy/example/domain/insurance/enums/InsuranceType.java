package com.revy.example.domain.insurance.enums;

import com.revy.example.common.enums.ExposedEnum;
public enum InsuranceType implements ExposedEnum {
    LIFE,        // 생명보험
    HEALTH,      // 건강·의료
    AUTO,        // 자동차
    PROPERTY,    // 재산·화재
    TRAVEL,      // 여행자
    ANNUITY,     // 연금
    PET          // 반려동물
}
