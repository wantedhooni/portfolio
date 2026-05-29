package com.revy.example.domain.pg.enums;

import com.revy.example.common.enums.ExposedEnum;

public enum PaymentMethod implements ExposedEnum {
    CARD,           // 신용·체크카드
    BANK_TRANSFER,  // 실시간 계좌이체
    KAKAO_PAY,      // 카카오페이
    NAVER_PAY,      // 네이버페이
    TOSS_PAY,       // 토스페이
    VIRTUAL_ACCOUNT // 가상계좌
}
