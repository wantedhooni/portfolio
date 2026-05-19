package com.revy.example.domain.account.enums;

public enum AccountStatus {
    ACTIVE,     // 정상 운용 중
    SUSPENDED,  // 거래 정지 (출금·매매 불가)
    CLOSED      // 해지 완료 (이력 보존용)
}