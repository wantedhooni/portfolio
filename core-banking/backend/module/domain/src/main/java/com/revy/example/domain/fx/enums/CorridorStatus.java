package com.revy.example.domain.fx.enums;

import com.revy.example.common.enums.ExposedEnum;

public enum CorridorStatus implements ExposedEnum {

    /** 정상 운영 — 환전 가능 */
    ACTIVE   ("활성"),
    /** 비활성 — 일시 중단 (운영팀 수동 제어) */
    INACTIVE ("비활성"),
    /** 정지 — 리스크 등 사유로 강제 중단 */
    SUSPENDED("정지");

    private final String label;

    CorridorStatus(String label) { this.label = label; }

    @Override
    public String getDefaultMessage() { return label; }
}
