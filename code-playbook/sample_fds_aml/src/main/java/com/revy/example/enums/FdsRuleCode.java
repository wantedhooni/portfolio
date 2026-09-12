package com.revy.example.enums;

public enum FdsRuleCode {
    VELOCITY_COUNT,       // 단시간 다수 거래
    VELOCITY_AMOUNT,      // 단시간 고액 누적
    GEO_SUSPICIOUS,       // 의심 국가/지역
    GEO_IMPOSSIBLE_TRAVEL,// 물리적 불가능한 이동
    BLACKLIST_ACCOUNT,    // 블랙리스트 계좌
    BLACKLIST_IP,         // 블랙리스트 IP
    DORMANT_ACTIVATE,     // 휴면계좌 갑작스런 활성화
    AMOUNT_SPLIT,         // 분할 거래 의심 (Structuring)
    TIME_ANOMALY          // 비정상 시간대 거래
}