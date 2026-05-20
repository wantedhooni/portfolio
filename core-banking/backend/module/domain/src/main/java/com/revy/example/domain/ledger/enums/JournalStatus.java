package com.revy.example.domain.ledger.enums;

public enum JournalStatus {
    DRAFT,        // 임시 (전기 전)
    POSTED,       // 전기 완료 (장부 반영)
    REVERSED      // 역분개 처리됨 (오류 정정)
}
