package com.revy.securities.domain.account.enums;

public enum AccountStatus {
    ACTIVE,     // 정상 사용 가능
    SUSPENDED,  // 거래 정지(제재/이상거래/본인확인 미완료 등)
    CLOSED      // 해지/종료
}
