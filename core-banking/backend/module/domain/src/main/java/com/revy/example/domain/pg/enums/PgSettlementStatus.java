package com.revy.example.domain.pg.enums;

import com.revy.example.common.enums.ExposedEnum;

public enum PgSettlementStatus implements ExposedEnum {
    PENDING,  // 정산 대기
    SETTLED,  // 정산 완료 (가맹점 계좌 입금)
    FAILED    // 정산 실패
}
